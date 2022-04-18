package com.artuhanau.ecobot.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CommandResult
{
    private Object sendAction;

    private Status status;

    private String message;
}
