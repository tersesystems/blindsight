# https://twineworks.github.io/tweakflow/reference.html
#
# XXX need to set the level to INFO constant

library blindsight {

  # level: the result of org.slf4j.event.Level.toInt()
  # enc: <class>.<method> i.e. com.tersesystems.blindsight.groovy.Main.logDebugSpecial
  # line: line number of the source code where condition was created
  # file: absolute path of the file containing the condition
  #
  doc 'Evaluates a condition'
  function evaluate: (long level, string enc, long line, string file) ->
     (level >= 20); # info_int = 20
}
