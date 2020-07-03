package com.tersesystems.blindsight

/**
 * An underlying statement immediately before being sent to SLF4J.
 */
final case class Entry(
    marker: Option[org.slf4j.Marker],
    message: String,
    args: Array[Any]
)
