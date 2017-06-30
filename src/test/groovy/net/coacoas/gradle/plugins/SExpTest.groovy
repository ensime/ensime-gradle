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

import org.junit.Test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat

public class SExpTest {
  @Test
  public void testMap() throws Exception {
    def settings = [ 'root-dir' : 'bob' ]
    assertThat(SExp.format(settings), equalTo('(:root-dir "bob")'))
  }

  @Test
  public void testList() throws Exception {
    def settings = [ 'bob', 'rita', 'sue' ]
    assertThat(SExp.format(settings), equalTo('("bob" "rita" "sue")'))
  }

  @Test
  public void testWindowsBackslashesAreEscaped() {
    def setting = [ bob : ["c:\\a.jar", "c:\\b.jar" ] ]
    assertThat(SExp.format(setting), both(containsString("c:\\\\a.jar")).and(containsString("c:\\\\b.jar")))
  }

  @Test
  public void testEmptyList() {
    assertThat(SExp.format([]), is("()"))
  }

  @Test
  public void testEmptyMap() {
    assertThat(SExp.format([:]), is("()"))
  }
}
