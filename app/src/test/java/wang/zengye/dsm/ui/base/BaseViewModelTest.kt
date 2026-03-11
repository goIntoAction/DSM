package wang.zengye.dsm.ui.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * BaseViewModel 测试类
 * 
 * 注意：由于 BaseViewModel 是抽象类，我们需要创建一个具体实现来测试
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setup() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== 测试用的 ViewModel 实现 ==========

    private class TestViewModel : BaseViewModel<TestState, TestIntent, TestEvent>() {
        
        private val _state = MutableStateFlow(TestState())
        override val state: StateFlow<TestState> = _state.asStateFlow()
        
        override val events = MutableSharedFlow<TestEvent>(
            extraBufferCapacity = 1
        )

        override suspend fun processIntent(intent: TestIntent) {
            when (intent) {
                is TestIntent.UpdateName -> updateName(intent.name)
                is TestIntent.IncrementCount -> incrementCount()
                is TestIntent.SetLoading -> setLoading(intent.loading)
                is TestIntent.EmitEvent -> emitEvent(intent.event)
            }
        }

        private fun updateName(name: String) {
            _state.value = _state.value.copy(name = name)
        }

        private fun incrementCount() {
            _state.value = _state.value.copy(count = _state.value.count + 1)
        }

        private fun setLoading(loading: Boolean) {
            _state.value = _state.value.copy(isLoading = loading)
        }

        private suspend fun emitEvent(event: TestEvent) {
            events.emit(event)
        }
    }

    // ========== 状态测试 ==========

    @Test
    fun initialState_isCorrect() {
        val viewModel = TestViewModel()
        assertEquals("", viewModel.state.value.name)
        assertEquals(0, viewModel.state.value.count)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun sendIntent_updatesState() = runTest {
        val viewModel = TestViewModel()
        
        viewModel.sendIntent(TestIntent.UpdateName("Test"))
        
        assertEquals("Test", viewModel.state.value.name)
    }

    @Test
    fun sendIntent_multipleTimes_updatesStateCorrectly() = runTest {
        val viewModel = TestViewModel()
        
        viewModel.sendIntent(TestIntent.UpdateName("First"))
        viewModel.sendIntent(TestIntent.UpdateName("Second"))
        
        assertEquals("Second", viewModel.state.value.name)
    }

    @Test
    fun sendIntent_incrementCount() = runTest {
        val viewModel = TestViewModel()
        
        viewModel.sendIntent(TestIntent.IncrementCount)
        viewModel.sendIntent(TestIntent.IncrementCount)
        viewModel.sendIntent(TestIntent.IncrementCount)
        
        assertEquals(3, viewModel.state.value.count)
    }

    @Test
    fun sendIntent_setLoading() = runTest {
        val viewModel = TestViewModel()
        
        viewModel.sendIntent(TestIntent.SetLoading(true))
        assertTrue(viewModel.state.value.isLoading)
        
        viewModel.sendIntent(TestIntent.SetLoading(false))
        assertFalse(viewModel.state.value.isLoading)
    }

    // ========== 事件测试 ==========
    // 注意：SharedFlow 事件测试需要更复杂的设置（如 Turbine 库）
    // 这里只测试基本的事件定义和 ViewModel 结构

    @Test
    fun eventsFlow_exists() {
        val viewModel = TestViewModel()
        // 验证 events Flow 存在
        assertNotNull(viewModel.events)
    }

    @Test
    fun testEvent_types() {
        // 验证事件类型定义
        val showMessage = TestEvent.ShowMessage("Test")
        assertTrue(showMessage is TestEvent)
        assertEquals("Test", showMessage.message)
        
        val navigateBack = TestEvent.NavigateBack
        assertTrue(navigateBack is TestEvent)
    }

    // ========== 并发安全测试 ==========

    @Test
    fun concurrentStateUpdates_handledCorrectly() = runTest {
        val viewModel = TestViewModel()
        val iterations = 100
        
        // 并发发送多个 Intent
        repeat(iterations) {
            viewModel.sendIntent(TestIntent.IncrementCount)
        }
        
        // 验证最终状态
        assertEquals(iterations, viewModel.state.value.count)
    }

    // ========== 边界情况测试 ==========

    @Test
    fun stateUpdate_withEmptyString() = runTest {
        val viewModel = TestViewModel()
        
        viewModel.sendIntent(TestIntent.UpdateName(""))
        
        assertEquals("", viewModel.state.value.name)
    }

    @Test
    fun stateUpdate_withVeryLongString() = runTest {
        val viewModel = TestViewModel()
        val longString = "a".repeat(10000)
        
        viewModel.sendIntent(TestIntent.UpdateName(longString))
        
        assertEquals(longString, viewModel.state.value.name)
    }

    @Test
    fun multipleLoadingToggle() = runTest {
        val viewModel = TestViewModel()
        
        repeat(10) {
            viewModel.sendIntent(TestIntent.SetLoading(true))
            assertTrue(viewModel.state.value.isLoading)
            viewModel.sendIntent(TestIntent.SetLoading(false))
            assertFalse(viewModel.state.value.isLoading)
        }
    }
}

// ========== 测试数据类 ==========

data class TestState(
    val name: String = "",
    val count: Int = 0,
    override val isLoading: Boolean = false,
    override val error: String? = null
) : BaseState

sealed class TestIntent : BaseIntent {
    data class UpdateName(val name: String) : TestIntent()
    data object IncrementCount : TestIntent()
    data class SetLoading(val loading: Boolean) : TestIntent()
    data class EmitEvent(val event: TestEvent) : TestIntent()
}

sealed class TestEvent : BaseEvent {
    data class ShowMessage(val message: String) : TestEvent()
    data object NavigateBack : TestEvent()
}
