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
