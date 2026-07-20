package com.example.util

import com.example.data.DailyStudyEntry

object ExcelCsvHelper {
    private val headers = listOf(
        "Date", "Study Hours", "Subject", "Topics", "Questions Solved",
        "PYQs", "Mock Tests", "Revision Hours", "Notes Prepared",
        "Confidence", "Productivity", "Remarks", "Preparation Score"
    )

    fun exportToCsv(entries: List<DailyStudyEntry>): String {
        val sb = StringBuilder()
        sb.append(headers.joinToString(","))
        sb.append("\n")

        for (entry in entries) {
            val row = listOf(
                entry.date,
                entry.studyHours.toString(),
                escapeSpecialCharacters(entry.subjectsStudied),
                escapeSpecialCharacters(entry.topicsCompleted),
                entry.questionsSolved.toString(),
                entry.pyqsSolved.toString(),
                entry.mockTestsAttempted.toString(),
                entry.revisionHours.toString(),
                if (entry.notesPrepared) "Yes" else "No",
                entry.confidence.toString(),
                entry.productivity.toString(),
                escapeSpecialCharacters(entry.remarks),
                entry.gpsScore.toString()
            )
            sb.append(row.joinToString(","))
            sb.append("\n")
        }
        return sb.toString()
    }

    private fun escapeSpecialCharacters(text: String): String {
        var escaped = text.replace("\"", "\"\"")
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            escaped = "\"$escaped\""
        }
        return escaped
    }

    fun importFromCsv(csvContent: String): List<DailyStudyEntry> {
        val entries = mutableListOf<DailyStudyEntry>()
        val lines = csvContent.split("\n")
        if (lines.size <= 1) return emptyList()

        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue

            try {
                val tokens = parseCsvLine(line)
                if (tokens.size < 13) continue

                val date = tokens[0]
                val studyHours = tokens[1].toDoubleOrNull() ?: 0.0
                val subject = tokens[2]
                val topics = tokens[3]
                val questionsSolved = tokens[4].toIntOrNull() ?: 0
                val pyqs = tokens[5].toIntOrNull() ?: 0
                val mockTests = tokens[6].toIntOrNull() ?: 0
                val revisionHours = tokens[7].toDoubleOrNull() ?: 0.0
                val notesPrepared = tokens[8].equals("Yes", ignoreCase = true)
                val confidence = tokens[9].toIntOrNull() ?: 5
                val productivity = tokens[10].toIntOrNull() ?: 5
                val remarks = tokens[11]
                val gpsScore = tokens[12].toIntOrNull() ?: 50

                entries.add(
                    DailyStudyEntry(
                        date = date,
                        studyHours = studyHours,
                        subjectsStudied = subject,
                        topicsCompleted = topics,
                        questionsSolved = questionsSolved,
                        pyqsSolved = pyqs,
                        mockTestsAttempted = mockTests,
                        revisionHours = revisionHours,
                        videoLectureHours = 0.0,
                        notesPrepared = notesPrepared,
                        confidence = confidence,
                        productivity = productivity,
                        remarks = remarks,
                        gpsScore = gpsScore
                    )
                )
            } catch (e: Exception) {
                // Ignore individual malformed entries
            }
        }
        return entries
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        var curVal = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            if (inQuotes) {
                if (ch == '\"') {
                    if (i + 1 < line.length && line[i + 1] == '\"') {
                        curVal.append('\"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    curVal.append(ch)
                }
            } else {
                if (ch == '\"') {
                    inQuotes = true
                } else if (ch == ',') {
                    tokens.add(curVal.toString().trim())
                    curVal = StringBuilder()
                } else {
                    curVal.append(ch)
                }
            }
            i++
        }
        tokens.add(curVal.toString().trim())
        return tokens
    }
}
