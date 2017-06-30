/*
 *    Copyright 2017 Bill Carlson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
