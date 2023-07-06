package com.example.coroutines_flow

import android.content.Context
import android.os.Bundle

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.coroutines_flow.ui.theme.Coroutines_FlowTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val list = (0..40).map {
            Item(
                height = Random.nextInt(540).dp,
                color = Color(Random.nextInt())
            )
        }





        setContent {
            Coroutines_FlowTheme {


                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val lazyColumnState = rememberLazyListState()
                    SliderLazyColumn(
                        key = list,
                        state = lazyColumnState,
                        context = LocalContext.current
                    ) {
                        items(list) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .height(item.height)
                                    .background(color = item.color)
                            ) {
                                Text(text = "BOX ${item.height}")
                            }
                        }
                        items(10){
                            Image(painter = painterResource(id = R.drawable.ic_launcher_background), contentDescription ="",
                                modifier = Modifier.padding(16.dp))
                        }
                        items(list, key = { it.height.value * it.color.toArgb() }) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .height(item.height)
                                    .background(color = item.color)
                            ) {
                                Text(text = "BOX ${item.height}")
                            }
                        }
                        items(10){
                            Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription ="",
                            modifier = Modifier.padding(16.dp))
                        }


                    }
                }
            }
        }
    }
}


@Composable
fun SliderLazyColumn(
    key: Any?,
    state: LazyListState,
    context: Context,
    content: LazyListScope.() -> Unit
) {
    suspend fun getStages(lazyListState: LazyListState, density: Float): List<Dp> {
        val stages = mutableListOf<Dp>()
        stages.add(0.dp)
        var height: Dp = 0.dp
        lazyListState.scrollToItem(lazyListState.layoutInfo.totalItemsCount)
        val lastOrPreLastItemIndex = lazyListState.firstVisibleItemIndex
        val offsetLast = (lazyListState.firstVisibleItemScrollOffset / density).dp

        for (item in 0 until lastOrPreLastItemIndex) {
            lazyListState.scrollToItem(item)

            val currentHeight =
                (lazyListState.layoutInfo.visibleItemsInfo.first().size / density).roundToInt().dp
            stages.add(height + currentHeight)
            height += currentHeight
        }
        stages.add(height + offsetLast)
        return stages
    }

    val density = remember { context.resources.displayMetrics.density }

    val pointSize = remember {
        mutableStateOf(40.dp)
    }
    val absoluteHeightDp = remember {
        mutableStateOf(1.dp)
    }
    val stagesOfDp = remember {
        mutableStateOf(mutableListOf(0.dp))
    }
    val heightScreen = LocalConfiguration.current.screenHeightDp


    val rowAlpha = animateFloatAsState(
        targetValue = if (stagesOfDp.value.size > 0) {
            1f
        } else {
            0f
        }, animationSpec = tween(300)
    )
    LaunchedEffect(key) {

        if (state.layoutInfo.totalItemsCount > 0) {
            CoroutineScope(Dispatchers.Main).launch {
                stagesOfDp.value = getStages(lazyListState = state, density) as MutableList<Dp>
                absoluteHeightDp.value = stagesOfDp.value.last().value.dp
                state.scrollToItem(0)
            }
        }
        Toast.makeText(context, "list refresh", Toast.LENGTH_SHORT).show()

    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .alpha(rowAlpha.value)
    ) {
        val firstVisibleItemScrollOffset = remember {
            derivedStateOf { state.firstVisibleItemScrollOffset }
        }
        val firstVisibleItemIndex = remember {
            derivedStateOf { state.firstVisibleItemIndex }
        }
        val multiplier = remember {
            derivedStateOf { heightScreen.toFloat() / absoluteHeightDp.value.value }
        }


        val sliderOffset = remember {
            mutableStateOf(Offset(0f, 0f))
        }


        if (stagesOfDp.value.size > 0) {
            sliderOffset.value = animateOffsetAsState(
                targetValue = if (!firstVisibleItemIndex.value.dp.value.isNaN() && !stagesOfDp.value[firstVisibleItemIndex.value].value.isNaN()
                    && !multiplier.value.isInfinite()
                ) {
                    Offset(
                        x = 0f,
                        y = (firstVisibleItemScrollOffset.value.dp.value / density + stagesOfDp.value[firstVisibleItemIndex.value].value) * multiplier.value
                    )
                } else {
                    Offset(0f, 0f)
                },
                animationSpec = tween(40)
            ).value
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(0.97f)
                .fillMaxHeight(), state = state
        ) {
            content.invoke(this)
        }

        val worstOffset = remember {
            derivedStateOf {
                if (!sliderOffset.value.y.dp.value.isNaN() && !absoluteHeightDp.value.value.isNaN() && absoluteHeightDp.value.value != 0f)
                    (pointSize.value * (sliderOffset.value.y.dp / heightScreen.dp))
                else {
                    0.dp
                }
            }
        }


        Box(
            modifier = Modifier
                .offset(
                    x = 0.dp,
                    y = sliderOffset.value.y.dp - worstOffset.value
                )
                .fillMaxWidth()
                .height(pointSize.value)

                .background(color = Color(0xBF666666))
        )
    }

}



