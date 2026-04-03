package ru.practicum.stats.exception;

public class CommentNotFoundException extends NotFoundException {

    public CommentNotFoundException(Long id) {
        super("Comment not found with id: " + id);
    }

    public CommentNotFoundException(String message) {
        super(message);
    }
}