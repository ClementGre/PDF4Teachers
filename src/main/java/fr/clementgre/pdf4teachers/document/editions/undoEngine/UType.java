/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.undoEngine;

public enum UType{
    // This action will be placed in the undo stack
    UNDO,
    // This action will be placed in the undo stack
    // but will not count for one undo action.
    // It will be followed by the next UNDO action.
    NO_COUNT,
    // This action will not be put in the undo stack
    NO_UNDO
}
