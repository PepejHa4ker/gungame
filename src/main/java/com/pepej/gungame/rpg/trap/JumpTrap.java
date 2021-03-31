package com.pepej.gungame.rpg.trap;

import com.pepej.gungame.user.User;

public class JumpTrap extends TrapBase {
    protected JumpTrap() {
        super("jump", TrapType.JUMP);
    }

    @Override
    public void onActivate(User user) {

    }
}
