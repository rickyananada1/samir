package com.samir.test.Test.Samir.model;

import com.samir.test.Test.Samir.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private boolean completed = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
