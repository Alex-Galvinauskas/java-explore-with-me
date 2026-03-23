package ru.practicum.stats.validation.event;

import org.springframework.stereotype.Component;
import ru.practicum.stats.exception.ValidationException;

@Component
public class EventFieldValidator {

    private static final int ANNOTATION_MIN_LENGTH = 20;
    private static final int ANNOTATION_MAX_LENGTH = 2000;
    private static final int DESCRIPTION_MIN_LENGTH = 20;
    private static final int DESCRIPTION_MAX_LENGTH = 7000;
    private static final int TITLE_MIN_LENGTH = 3;
    private static final int TITLE_MAX_LENGTH = 120;

    public void validateAnnotation(String annotation) {
        if (annotation.length() < ANNOTATION_MIN_LENGTH || annotation.length() > ANNOTATION_MAX_LENGTH) {
            throw new ValidationException(
                    String.format("Аннотация должна быть от %d до %d символов",
                            ANNOTATION_MIN_LENGTH, ANNOTATION_MAX_LENGTH)
            );
        }
    }

    public void validateDescription(String description) {
        if (description.length() < DESCRIPTION_MIN_LENGTH || description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new ValidationException(
                    String.format("Описание должно быть от %d до %d символов",
                            DESCRIPTION_MIN_LENGTH, DESCRIPTION_MAX_LENGTH)
            );
        }
    }

    public void validateTitle(String title) {
        if (title.length() < TITLE_MIN_LENGTH || title.length() > TITLE_MAX_LENGTH) {
            throw new ValidationException(
                    String.format("Заголовок должен быть от %d до %d символов",
                            TITLE_MIN_LENGTH, TITLE_MAX_LENGTH)
            );
        }
    }

    public void validateParticipantLimit(Integer limit) {
        if (limit != null && limit < 0) {
            throw new ValidationException("Лимит участников не может быть отрицательным");
        }
    }
}