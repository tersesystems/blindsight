package com.tersesystems.blindsight

/**
 * An underlying statement immediately before being sent to SLF4J.
 *
 * @param marker the slf4j marker, if any
 * @param message the string message format
 * @param args the array of arguments, or `Array.empty` if none.
 */
case class LogEntry(marker: Option[org.slf4j.Marker], message: String, args: Array[Any])
