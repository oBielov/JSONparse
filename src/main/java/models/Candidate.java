package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Candidate {
    int id;
    String candidateName;
    int extraTime;
}
