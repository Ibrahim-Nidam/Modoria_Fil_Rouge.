package com.modoria.domain.chat.controller;

import com.modoria.domain.chat.dto.request.CreateChatRoomRequest;
import com.modoria.domain.chat.dto.request.EscalationRequest;
import com.modoria.domain.chat.dto.request.SendMessageRequest;
import com.modoria.domain.chat.dto.response.ChatMessageResponse;
import com.modoria.domain.chat.dto.response.ChatRoomResponse;
import com.modoria.infrastructure.common.dto.ApiResponse;
import com.modoria.domain.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for chat endpoints.
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat Support API")
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new chat room")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createRoom(@Valid @RequestBody CreateChatRoomRequest request) {
        log.info("Request to create chat room: {}", request.getSubject());
        ChatRoomResponse response = chatService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Chat room created successfully"));
    }

    @GetMapping("/rooms/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get chat room by ID")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getRoomById(@PathVariable Long id) {
        ChatRoomResponse response = chatService.getRoomById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/rooms/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's chat rooms")
    public ResponseEntity<ApiResponse<Page<ChatRoomResponse>>> getCurrentUserRooms(
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ChatRoomResponse> response = chatService.getCurrentUserRooms(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/rooms/unassigned")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN')")
    @Operation(summary = "Get unassigned chat rooms (Support)")
    public ResponseEntity<ApiResponse<Page<ChatRoomResponse>>> getUnassignedRooms(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ChatRoomResponse> response = chatService.getUnassignedRooms(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/rooms/assigned")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN')")
    @Operation(summary = "Get rooms assigned to current agent (Support)")
    public ResponseEntity<ApiResponse<Page<ChatRoomResponse>>> getSupportAgentRooms(
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ChatRoomResponse> response = chatService.getSupportAgentRooms(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/rooms/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get messages for a room")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getRoomMessages(@PathVariable Long id) {
        List<ChatMessageResponse> response = chatService.getRoomMessages(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/rooms/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Send a message to a room")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable Long id,
            @Valid @RequestBody SendMessageRequest request) {
        ChatMessageResponse response = chatService.sendMessage(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Message sent successfully"));
    }

    @PostMapping("/rooms/{id}/assign")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN')")
    @Operation(summary = "Assign room to self (Support)")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> assignAgent(@PathVariable Long id) {
        ChatRoomResponse response = chatService.assignAgent(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Room assigned successfully"));
    }

    @PostMapping("/rooms/{id}/escalate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Request human support escalation")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> requestEscalation(
            @PathVariable Long id,
            @Valid @RequestBody EscalationRequest request) {
        log.info("Requesting escalation for room {}", id);
        ChatRoomResponse response = chatService.requestEscalation(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Escalation requested successfully"));
    }

    @PostMapping("/rooms/{id}/resolve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark room as resolved")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> resolveRoom(@PathVariable Long id) {
        ChatRoomResponse response = chatService.resolveRoom(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Room resolved successfully"));
    }

    @PostMapping("/rooms/{id}/close")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Close chat room")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> closeRoom(@PathVariable Long id) {
        ChatRoomResponse response = chatService.closeRoom(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Room closed successfully"));
    }

    @PostMapping("/rooms/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark messages as read")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(@PathVariable Long id) {
        chatService.markMessagesAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Messages marked as read"));
    }

    @GetMapping("/rooms/{id}/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get unread message count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable Long id) {
        long count = chatService.getUnreadCount(id);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
