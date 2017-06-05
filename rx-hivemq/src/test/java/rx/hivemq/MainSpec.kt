package rx.hivemq

import com.google.common.base.Optional
import com.hivemq.spi.security.ClientData
import com.hivemq.spi.security.SslClientCertificate
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
//import kotlin.test.assertEquals
//import kotlin.test.todo
import org.mockito.Mockito.mock
import org.mockito.Mockito
import org.mockito.Mockito.verify

/**
 * Created by yongjhih on 6/4/17.
 */
@RunWith(JUnitPlatform::class)
class MainSpec : Spek({
    var counter = 0
    beforeEachTest {
        counter++
    }

    describe("a number") {
        it ("should be 1") {
            assertThat(counter).isEqualTo(1)
        }
    }
})
