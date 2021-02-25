# Backus Naur Form

XXX Write backus-naur form

NodeObject := MapEntry*

MapEntry := Key -> Value

Key := Term | IRI | Keyword

IRI := CompactIRI | FullIRI

Value := ValueObject | NodeObject | ListObject | SetObject

ValueObject := String | Number | Boolean | TypedValue | null

TypedValue := ???

ListObject := ???

SetObject := ???
