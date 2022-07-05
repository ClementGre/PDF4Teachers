/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills.parsers;

import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.EditionSkill;

import java.util.List;

public record StudentGrades(long studentId, String studentName, String fileName, List<EditionSkill> skills) {
}
