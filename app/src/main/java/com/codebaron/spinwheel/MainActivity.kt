package com.codebaron.spinwheel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codebaron.spinwheel.ui.theme.SpinWheelTheme
import com.codebaron.spinwheel.widget.SpinWheelWidget

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpinWheelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SpinWheelDemoScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SpinWheelDemoScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var widgetCount by remember { mutableIntStateOf(SpinWheelWidget.getWidgetCount(context)) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "Spin Wheel",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Widget Demo",
                fontSize = 18.sp,
                color = Color(0xFFe94560),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Widget icon placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = Color(0xFF0f3460),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎡",
                    fontSize = 64.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Widget status card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF16213e)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Widget Status",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9ca3af)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "$widgetCount",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFe94560)
                    )

                    Text(
                        text = if (widgetCount == 1) "Widget Active" else "Widgets Active",
                        fontSize = 14.sp,
                        color = Color(0xFF9ca3af)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Refresh button
            Button(
                onClick = {
                    SpinWheelWidget.refreshAllWidgets(context)
                    widgetCount = SpinWheelWidget.getWidgetCount(context)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFe94560)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Refresh Widgets",
                    modifier = Modifier.padding(vertical = 8.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF16213e).copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "How to Add Widget",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    InstructionStep(number = 1, text = "Long press on your home screen")
                    InstructionStep(number = 2, text = "Tap 'Widgets'")
                    InstructionStep(number = 3, text = "Find 'Spin Wheel' widget")
                    InstructionStep(number = 4, text = "Drag it to your home screen")
                    InstructionStep(number = 5, text = "Tap the wheel to spin!")
                }
            }
        }
    }
}

@Composable
fun InstructionStep(number: Int, text: String) {
    Text(
        text = "$number. $text",
        fontSize = 14.sp,
        color = Color(0xFF9ca3af),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun SpinWheelDemoScreenPreview() {
    SpinWheelTheme {
        SpinWheelDemoScreen()
    }
}