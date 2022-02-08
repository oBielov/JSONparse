package models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
public class JsonModel {
    int id;
    Meta meta;
    List<Candidate> candidates;
}
