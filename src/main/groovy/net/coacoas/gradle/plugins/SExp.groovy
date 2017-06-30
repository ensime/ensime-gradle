package net.coacoas.gradle.plugins

/**
 * Transform a configuration object into stringified Lisp expressions
 */
class SExp {
  static String format(Map map) { 
      '(' + map.entrySet().collect { ":${it.key} " + format(it.value)}.join('\n') + ')'
  }

  static String format(List list) { 
      '(' + list.collect { format(it) }.join(' ') + ')'
  }

  static String format(String string) { 
    "\"${string.replaceAll("\\\\", "\\\\\\\\")}\""
  }

  static String format(Boolean bool) {
    bool ? 't' : 'nil'
  }

  static String format(Integer integer) {
    "${integer}"
  }

  static String format(Object object) {
     "\"${object}\""
  }
}
