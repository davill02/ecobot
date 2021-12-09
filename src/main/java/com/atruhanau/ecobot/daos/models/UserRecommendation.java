package com.atruhanau.ecobot.daos.models;

import com.atruhanau.ecobot.daos.models.slots.Slot;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class UserRecommendation {
    private List<Slot> slots;

    public UserRecommendation() {
        this.slots = new ArrayList<>();
    }
}
