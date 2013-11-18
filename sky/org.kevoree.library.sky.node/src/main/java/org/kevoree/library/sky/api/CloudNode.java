package org.kevoree.library.sky.api;

import org.kevoree.annotation.*;


@PrimitiveCommands(value = {}, values = {CloudNode.REMOVE_NODE, CloudNode.ADD_NODE})
@NodeType
public interface CloudNode {
    public static final String REMOVE_NODE = "RemoveNode";
    public static final String ADD_NODE = "AddNode";
}
