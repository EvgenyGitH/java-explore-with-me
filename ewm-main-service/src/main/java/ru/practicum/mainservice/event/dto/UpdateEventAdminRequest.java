package ru.practicum.mainservice.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.mainservice.event.model.Location;
import ru.practicum.mainservice.event.model.StateAction;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000)
    private String annotation;
    private Long category;
    @Size(min = 20, max = 7000)
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private Location location;
    private Boolean paid; //default false
    private Long participantLimit; //default: 0
    private Boolean requestModeration; //default: true
    @Enumerated(EnumType.STRING)
    private StateAction stateAction;  //Enum: PUBLISH_EVENT, REJECT_EVENT //SEND_TO_REVIEW, CANCEL_REVIEW
    @Size(min = 3, max = 120)
    private String title;
}
