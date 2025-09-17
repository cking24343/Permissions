package com.sample.permissions.module.ui.permissions

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.permissions.dialogs.model.UiRequestResult
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sample.permissions.module.R

/** Sample custom rationale without optional UiSpec (ie create our own values for layout) */
@Composable
fun LocationFullscreenRationale(
    modifier: Modifier = Modifier,
    logo: @Composable (() -> Unit)? = null,
    featureGraphic: @Composable (() -> Unit)? = { SampleGraphic() },
    result: (UiRequestResult) -> Unit = {},
) {
    val appName = "Your App"
    val permissionHeader = "Find what matters most, when you need it."
    val sectionTitle = "$appName needs the following permission"
    val permissionName = "Location"
    val descriptionIntro = "$appName may use your location data for the following:"
    val finePrint = "$appName will never share this information with any 3rd party."

    val bulletItems: List<String> = listOf(
        "Location-appropriate messages",
        "Suggesting specific data near you",
        "Calculating distances and routes",
        "Alerting you when you arrive at a destination, even when the app is closed or not in use",
    )

    val blue = Color(0xFF1E88E5)
    val bg = Color(0xFFFCFCFC)

    Scaffold(
        modifier = modifier,
        containerColor = bg,
        bottomBar = {
            // Fixed CTA bar
            Surface(shadowElevation = 8.dp, color = bg) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { result(UiRequestResult.Dismissed) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = blue
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = ButtonDefaults.outlinedButtonBorder.brush
                        )
                    ) {
                        Text("Not now")
                    }
                    Button(
                        onClick = { result(UiRequestResult.Confirmed) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonColors(
                            containerColor = blue,
                            contentColor = Color.White,
                            disabledContentColor = blue,
                            disabledContainerColor = blue
                        )
                    ) {
                        Text("Agree")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand/logo (centered)
            if (logo != null) {
                Box(Modifier.padding(top = 8.dp, bottom = 16.dp)) { logo() }
            }

            // Large header (centered)
            Text(
                text = permissionHeader,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            // Feature graphic / icon cluster
            if (featureGraphic != null) {
                Box(
                    Modifier
                        .padding(top = 20.dp, bottom = 12.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { featureGraphic() }
            }

            // Section title (left-aligned)
            Text(
                text = sectionTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            // Permission name (sub-header)
            Text(
                text = permissionName,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 6.dp)
            )

            // Intro body text
            Text(
                text = descriptionIntro,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            // Bulleted list
            Spacer(Modifier.height(8.dp))
            BulletList(items = bulletItems)

            // Fine print
            Text(
                text = finePrint,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )

            // Bottom spacer so last line isn’t hidden behind the button bar
            Spacer(Modifier.height(96.dp))
        }
    }
}

@Composable
private fun BulletList(items: List<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        items.forEach { line ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("•", Modifier.padding(end = 8.dp))
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


@Composable
fun SampleLogo() {
    Image(
        painter = painterResource(R.drawable.ic_sample_logo),
        contentDescription = "sample logo",
    )
}

@Composable
fun SampleGraphic() {
    Image(
        painter = painterResource(R.drawable.ic_sample_graphic),
        contentDescription = "sample logo",
    )
}

@Preview
@Composable
fun PreviewLocationFullscreenRationale() {
    LocationFullscreenRationale()
}