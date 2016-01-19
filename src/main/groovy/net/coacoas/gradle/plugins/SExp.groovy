package net.coacoas.gradle.plugins

/**
 * Transform a configuration object into stringified Lisp expressions
 */
class SExp {
  static String format(Object object) {
    if (object instanceof Map) {
      return object.empty ? 'nil' : '(' + object.entrySet().collect { ":${it.key} " + format(it.value)}.join('\n') + ')'
    } else if (object instanceof List) {
      return object.empty ? 'nil' : '(' + object.collect { format(it) }.join(' ') + ')'
    } else if (object instanceof String) {
      return '"' + object.toString().replaceAll("\\\\", "\\\\\\\\") + '"'
    } else if (object instanceof Boolean) {
      return object ? "t" : "nil"
    } else if (object instanceof Integer) {
      return "${object}"
    } else {
      return "\"${object}\""
    }
  }
}
