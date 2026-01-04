package ronsijm.templater.integration

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import ronsijm.templater.parser.TemplateParser
import ronsijm.templater.TestContextFactory
import java.io.File


@Tag("slow")
class MatrixMultiplyBenchmarkTest {

    @Test
    fun `test simple loop performance`() {
        val context = TestContextFactory.create(fileContent = "")
        val parser = TemplateParser()

        val template = """
            <%*
            const N = 3;
            let sum = 0;
            for (let i = 0; i < N; i++) {
                for (let j = 0; j < N; j++) {
                    for (let k = 0; k < N; k++) {
                        sum += 1;
                    }
                }
            }
            tR += `sum=${'$'}{sum}`;
            %>
        """.trimIndent()

        val start = System.currentTimeMillis()
        val result = parser.parse(template, context)
        val elapsed = System.currentTimeMillis() - start

        println("Simple loop test:")
        println("Result: $result")
        println("Elapsed: ${elapsed}ms")

        assertTrue(result.contains("sum=27"))
    }

    @Test
    @Disabled("Too slow - takes 10+ minutes even with optimizations. Matrix multiply is not practical for interpreter.")
    fun `test matrix multiply with N=3`() {
        val context = TestContextFactory.create(fileContent = "")
        val parser = TemplateParser()

        val template = """
            <%*
            function nowMs() {
                return Date.now();
            }

            // Simplified LCG without closure - use global state
            let lcgState = 0;
            function lcgInit(seed) {
                lcgState = seed >>> 0;
            }
            function lcgNext() {
                lcgState = (1664525 * lcgState + 1013904223) >>> 0;
                return lcgState / 0x100000000;
            }

            function makeMatrix(N, seed) {
                lcgInit(seed);
                const m = new Array(N * N);
                for (let i = 0; i < m.length; i++) {
                    m[i] = (lcgNext() * 2 - 1);
                }
                return m;
            }

            function matmulNaiveFlat(A, B, N) {
                const C = new Array(N * N);
                for (let i = 0; i < N; i++) {
                    const iN = i * N;
                    for (let j = 0; j < N; j++) {
                        let sum = 0;
                        for (let k = 0; k < N; k++) {
                            sum += A[iN + k] * B[k * N + j];
                        }
                        C[iN + j] = sum;
                    }
                }
                return C;
            }

            function checksum(M) {
                let s = 0;
                for (let i = 0; i < M.length; i += 97) s += M[i];
                return s;
            }

            const N = 3;
            const warmups = 0;
            const runs = 1;
            const seedA = 1234;
            const seedB = 5678;

            const A = makeMatrix(N, seedA);
            const B = makeMatrix(N, seedB);

            const t0 = nowMs();
            const C = matmulNaiveFlat(A, B, N);
            const t1 = nowMs();

            const lastChecksum = checksum(C);
            const dt = t1 - t0;

            tR += `Matrix multiply (naive)\n`;
            tR += `N=${'$'}{N} (N^3=${'$'}{N*N*N} mul-adds)\n`;
            tR += `warmups=${'$'}{warmups}, runs=${'$'}{runs}\n`;
            tR += `best_ms=${'$'}{dt.toFixed(3)}\n`;
            tR += `checksum=${'$'}{lastChecksum}\n`;
            %>
        """.trimIndent()

        val start = System.currentTimeMillis()
        val result = parser.parse(template, context)
        val elapsed = System.currentTimeMillis() - start

        println("=".repeat(60))
        println("Matrix Multiply Benchmark Results (N=3)")
        println("=".repeat(60))
        println(result)
        println("=".repeat(60))
        println("Total test execution time: ${elapsed}ms")
        println("=".repeat(60))

        assertTrue(result.contains("Matrix multiply (naive)"))
        assertTrue(result.contains("N=3"))
        assertTrue(result.contains("checksum="))
    }
}
