package com.tersesystems.blindsight

case class RawStatement(marker: Option[org.slf4j.Marker], message: String, args: Array[Any])
