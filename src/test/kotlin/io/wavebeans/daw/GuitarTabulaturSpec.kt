package io.wavebeans.daw

import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.key
import assertk.assertions.prop
import assertk.assertions.size
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


object GuitarTabulaturSpec : Spek({
    describe("6 string guitar") {
        it("should parse one and two digits notes") {
            val tab = GuitarTabulatur.parse(
                """
                    # 1  2  3 4 
                    e|12--------
                    B|---1------
                    G|------3---
                    D|---12-----
                    A|------1---
                    E|--------2-
                """.trimIndent()
            )
            assertThat(tab).all {
                prop(GuitarTabulatur::keys).containsExactly("e", "B", "G", "D", "A", "E")
                prop(GuitarTabulatur::steps).all {
                    size().isEqualTo(5)
                    index(0).all {
                        key("e").isEqualTo(PickNote(12))
                        key("B").isEqualTo(Mute)
                        key("G").isEqualTo(Mute)
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(1).all {
                        key("e").isEqualTo(Mute)
                        key("B").isEqualTo(PickNote(1))
                        key("G").isEqualTo(Mute)
                        key("D").isEqualTo(PickNote(12))
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(2).all {
                        key("e").isEqualTo(Mute)
                        key("B").isEqualTo(Mute)
                        key("G").isEqualTo(PickNote(3))
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(PickNote(1))
                        key("E").isEqualTo(Mute)
                    }

                    index(3).all {
                        key("e").isEqualTo(Mute)
                        key("B").isEqualTo(Mute)
                        key("G").isEqualTo(Mute)
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(PickNote(2))
                    }
                    index(4).isEqualTo(Bar)
                }
            }
        }
        it("should parse the extra dash") {
            val tab = GuitarTabulatur.parse(
                """
                    # 1  2 3 4 
                    e|1--------
                    B|---2-----
                    G|-----0---
                    D|---------
                    A|---------
                    E|---------
                """.trimIndent()
            )
            assertThat(tab).all {
                prop(GuitarTabulatur::keys).containsExactly("e", "B", "G", "D", "A", "E")
                prop(GuitarTabulatur::steps).all {
                    size().isEqualTo(5)
                    index(0).all {
                        key("e").isEqualTo(PickNote(1))
                        key("B").isEqualTo(Mute)
                        key("G").isEqualTo(Mute)
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(1).all {
                        key("e").isEqualTo(Mute)
                        key("B").isEqualTo(PickNote(2))
                        key("G").isEqualTo(Mute)
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(2).all {
                        key("e").isEqualTo(Mute)
                        key("B").isEqualTo(Mute)
                        key("G").isEqualTo(PickNote(0))
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(3).all {
                        key("e").isEqualTo(Mute)
                        key("B").isEqualTo(Mute)
                        key("G").isEqualTo(Mute)
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }
                    index(4).isEqualTo(Bar)
                }
            }
        }
        it("should interpret notes not separated with dash line as a single step") {
            val tab = GuitarTabulatur.parse(
                """
                    # 1   2 3 4 
                    e|12--------
                    B|--1-------
                    G|----0-----
                    D|----------
                    A|----------
                    E|----------
                """.trimIndent()
            )
            assertThat(tab).all {
                prop(GuitarTabulatur::keys).containsExactly("e", "B", "G", "D", "A", "E")
                prop(GuitarTabulatur::steps).all {
                    size().isEqualTo(5)
                    index(0).all {
                        key("e").isEqualTo(PickNote(12))
                        key("B").isEqualTo(PickNote(1))
                        key("G").isEqualTo(Mute)
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(1).all {
                        key("e").isEqualTo(Mute)
                        key("B").isEqualTo(Mute)
                        key("G").isEqualTo(PickNote(0))
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(2).all {
                        key("e").isEqualTo(Mute)
                        key("B").isEqualTo(Mute)
                        key("G").isEqualTo(Mute)
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(3).all {
                        key("e").isEqualTo(Mute)
                        key("B").isEqualTo(Mute)
                        key("G").isEqualTo(Mute)
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }
                    index(4).isEqualTo(Bar)
                }
            }
        }
        it("should not parse the missing step as mute") {
            val tab = GuitarTabulatur.parse(
                """
                    # 1   2 3 4 
                    e|12------
                    B|--1-----
                    G|----0---
                    D|--------
                    A|--------
                    E|--------
                """.trimIndent()
            )
            assertThat(tab).all {
                prop(GuitarTabulatur::keys).containsExactly("e", "B", "G", "D", "A", "E")
                prop(GuitarTabulatur::steps).all {
                    size().isEqualTo(4)
                    index(0).all {
                        key("e").isEqualTo(PickNote(12))
                        key("B").isEqualTo(PickNote(1))
                        key("G").isEqualTo(Mute)
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(1).all {
                        key("e").isEqualTo(Mute)
                        key("B").isEqualTo(Mute)
                        key("G").isEqualTo(PickNote(0))
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(2).all {
                        key("e").isEqualTo(Mute)
                        key("B").isEqualTo(Mute)
                        key("G").isEqualTo(Mute)
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }
                    index(3).isEqualTo(Bar)
                }
            }
        }
        it("should also parse superfluous steps") {
            val tab = GuitarTabulatur.parse(
                """
                    # 1   2 3 4 5
                    e|12--------0-
                    B|--1-------1-
                    G|----0-----2-
                    D|----------3-
                    A|----------4-
                    E|----------5-
                """.trimIndent()
            )
            assertThat(tab).all {
                prop(GuitarTabulatur::keys).containsExactly("e", "B", "G", "D", "A", "E")
                prop(GuitarTabulatur::steps).all {
                    size().isEqualTo(6)
                    index(0).all {
                        key("e").isEqualTo(PickNote(12))
                        key("B").isEqualTo(PickNote(1))
                        key("G").isEqualTo(Mute)
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(1).all {
                        key("e").isEqualTo(Mute)
                        key("B").isEqualTo(Mute)
                        key("G").isEqualTo(PickNote(0))
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(2).all {
                        key("e").isEqualTo(Mute)
                        key("B").isEqualTo(Mute)
                        key("G").isEqualTo(Mute)
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(3).all {
                        key("e").isEqualTo(Mute)
                        key("B").isEqualTo(Mute)
                        key("G").isEqualTo(Mute)
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(4).all {
                        key("e").isEqualTo(PickNote(0))
                        key("B").isEqualTo(PickNote(1))
                        key("G").isEqualTo(PickNote(2))
                        key("D").isEqualTo(PickNote(3))
                        key("A").isEqualTo(PickNote(4))
                        key("E").isEqualTo(PickNote(5))
                    }
                    index(5).isEqualTo(Bar)
                }
            }
        }
        it("should parse superfluous steps before the bar") {
            val tab = GuitarTabulatur.parse(
                """
                    # 1   2 3 4 5 |1  2
                    e|12--------0-|10-
                    B|-1--------1-|11-
                    G|----0-----2-|12-
                    D|----------3-|13-
                    A|----------4-|14-
                    E|----------5-|15-
                """.trimIndent()
            )
            assertThat(tab).all {
                prop(GuitarTabulatur::keys).containsExactly("e", "B", "G", "D", "A", "E")
                prop(GuitarTabulatur::steps).all {
                    size().isEqualTo(8)
                    index(0).all {
                        key("e").isEqualTo(PickNote(12))
                        key("B").isEqualTo(PickNote(1))
                        key("G").isEqualTo(Mute)
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(1).all {
                        key("e").isEqualTo(Mute)
                        key("B").isEqualTo(Mute)
                        key("G").isEqualTo(PickNote(0))
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(2).all {
                        key("e").isEqualTo(Mute)
                        key("B").isEqualTo(Mute)
                        key("G").isEqualTo(Mute)
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(3).all {
                        key("e").isEqualTo(Mute)
                        key("B").isEqualTo(Mute)
                        key("G").isEqualTo(Mute)
                        key("D").isEqualTo(Mute)
                        key("A").isEqualTo(Mute)
                        key("E").isEqualTo(Mute)
                    }

                    index(4).all {
                        key("e").isEqualTo(PickNote(0))
                        key("B").isEqualTo(PickNote(1))
                        key("G").isEqualTo(PickNote(2))
                        key("D").isEqualTo(PickNote(3))
                        key("A").isEqualTo(PickNote(4))
                        key("E").isEqualTo(PickNote(5))
                    }

                    index(5).isEqualTo(Bar)

                    index(6).all {
                        key("e").isEqualTo(PickNote(10))
                        key("B").isEqualTo(PickNote(11))
                        key("G").isEqualTo(PickNote(12))
                        key("D").isEqualTo(PickNote(13))
                        key("A").isEqualTo(PickNote(14))
                        key("E").isEqualTo(PickNote(15))
                    }

                    index(7).isEqualTo(Bar)
                }
            }
        }
    }
})

private fun Assert<Step>.key(key: String): Assert<GuitarMotion> = prop(Notes::motions).key(key)
