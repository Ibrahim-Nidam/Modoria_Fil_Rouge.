package com.modoria.domain.support.service.impl;

import com.modoria.domain.chat.entity.ChatRoom;
import com.modoria.domain.chat.enums.ChatRoomStatus;
import com.modoria.domain.chat.enums.SupportType;
import com.modoria.domain.chat.mapper.ChatMapper;
import com.modoria.domain.chat.repository.ChatRoomRepository;
import com.modoria.domain.support.service.SupportQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportQueueServiceImpl implements SupportQueueService {

    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMapper chatMapper;

    // In-memory queue for speed, but ideally backed by Redis or DB status
    // For this implementation, we rely on DB status 'WAITING_AGENT' as source of
    // truth
    // but keep a cache for ordering?
    // Actually, simplest robust way is to query DB by WAITING_AGENT and sort by
    // Priority/Time

    @Override
    public void addToQueue(ChatRoom chatRoom) {
        log.info("Adding room {} to support queue", chatRoom.getId());

        // Notify all support agents
        messagingTemplate.convertAndSend("/topic/support/queue", chatMapper.toResponse(chatRoom));
    }

    @Override
    public void removeFromQueue(Long chatRoomId) {
        log.info("Removing room {} from support queue", chatRoomId);
        // Usually just handled by status change, but if we maintained in-memory list
        // we'd remove it.
        // Also notify dashboard that it's taken
        messagingTemplate.convertAndSend("/topic/support/queue/remove", chatRoomId);
    }

    @Override
    public int getQueuePosition(Long chatRoomId) {
        List<ChatRoom> queue = getQueuedRooms();
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getId().equals(chatRoomId)) {
                return i + 1;
            }
        }
        return -1;
    }

    @Override
    public List<ChatRoom> getQueuedRooms() {
        // Fetch all rooms with status WAITING_AGENT
        return chatRoomRepository.findAll().stream() // Ideally custom query
                .filter(r -> r.getSupportType() == SupportType.WAITING_AGENT && r.getStatus() == ChatRoomStatus.OPEN)
                .sorted(Comparator.comparing(ChatRoom::getPriority) // HIGH before NORMAL
                        .thenComparing(ChatRoom::getCreatedAt)) // Oldest first
                .collect(Collectors.toList());
    }

    @Override
    public String getEstimatedWaitTime(Long chatRoomId) {
        int pos = getQueuePosition(chatRoomId);
        if (pos <= 0)
            return "Unknown";

        // Simple heuristic: 5 mins per ticket ahead
        int minutes = pos * 5;
        return "~" + minutes + " min";
    }
}
