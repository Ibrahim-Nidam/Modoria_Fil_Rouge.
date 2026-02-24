package com.modoria.domain.support.service;

import com.modoria.domain.chat.entity.ChatRoom;

import java.util.List;

/**
 * Service to manage the queue of chat rooms waiting for human support agents.
 */
public interface SupportQueueService {

    /**
     * Add a chat room to the support queue.
     *
     * @param chatRoom The chat room to add
     */
    void addToQueue(ChatRoom chatRoom);

    /**
     * Remove a chat room from the queue (e.g., when assigned or closed).
     *
     * @param chatRoomId ID of the chat room
     */
    void removeFromQueue(Long chatRoomId);

    /**
     * Get the position of a chat room in the queue.
     *
     * @param chatRoomId ID of the chat room
     * @return 1-based index in queue, or -1 if not in queue
     */
    int getQueuePosition(Long chatRoomId);

    /**
     * Get all rooms currently in the queue.
     *
     * @return List of queued chat rooms
     */
    List<ChatRoom> getQueuedRooms();

    /**
     * Calculate estimated wait time.
     *
     * @param chatRoomId ID of the room
     * @return Estimated wait time string (e.g. "5 min")
     */
    String getEstimatedWaitTime(Long chatRoomId);
}
