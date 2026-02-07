package ru.agrachev.cookapp.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.agrachev.cookapp.presentation.theme.CookAppTheme

class MainActivity : ComponentActivity() {
    lateinit var viewModel: MainActivityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: MainActivityViewModel by viewModel()
        this.viewModel = viewModel
        enableEdgeToEdge()
        setContent {
            CookAppTheme {
                val snackbarHostState = remember {
                    SnackbarHostState()
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                ) { innerPadding ->
                    val scope = rememberCoroutineScope()
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Hello")
                            }
                        },
                    )
                }
            }
        }
        addOnNewIntentListener { intent ->
            with(intent) {
                if (action == Intent.ACTION_SEND && type == "text/plain") {
                    getStringExtra(Intent.EXTRA_TEXT)?.let {
                        viewModel.parseUrl(it)
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier, onClick: () -> Unit = { }) {
    val aa = rememberDrawerState(DrawerValue.Closed)
    val callback by rememberUpdatedState(onClick)
    /*ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (view1, view2, view3) = createRefs()
        Text(text = "ddd", modifier = Modifier.constrainAs(view1) {
            top.linkTo(parent.top, margin = 16.dp)
            linkTo(parent.start, parent.end, bias = 0.25f)
        })
    }*/
    ConstraintLayout(aaa(), modifier = Modifier.fillMaxSize()) {
        Text(text = "aaa", modifier = Modifier.layoutId("a"))
        Text(text = "bbb", modifier = Modifier.layoutId("b"))
    }
    val listState = rememberLazyListState()
    val items by remember {
        mutableStateOf((0..100).toList())
    }
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(16.dp, 16.dp)
    ) {
        items(items, key = { it.hashCode() }) { item ->
            Text(item.toString())
        }
    }/*{
        /*val items by remember {
            mutableStateOf("")
           // (0..100).toList()
        }*/
        //items()
    }*/
    /*Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        LazyColumn(
            modifier = modifier
                .clip(RoundedCornerShape(10.dp))
        ) {
            items(items = (0..100).toList(), key = { o -> o }) { item ->
                Text(item.toString())
            }
        }
        Button(
            content = {
                Text(
                    text = "Hello $name!",
                    modifier = Modifier,
                )
            },
            onClick = callback
        )
    }*/
}

fun aaa() = ConstraintSet {
    val a = createRefFor("a")
    val b = createRefFor("b")
    constrain(a) {
        start.linkTo(parent.start, margin = 16.dp)
        top.linkTo(parent.top, margin = 16.dp)
    }
    constrain(b) {
        linkTo(parent.start, parent.end, bias = 0.25f)
        linkTo(parent.top, parent.bottom, bias = 0.25f)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CookAppTheme {
        Greeting("Android")
    }
}
