package com.github.jibbo.norwegiantraining.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun InviteFriendScreen(
    inviteCode: String = "FIT-JOIN-482",
    onInviteClick: () -> Unit = {},
    onCopyCode: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            text = "Invite a Friend",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Working out together makes it easier to stay consistent.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Illustration placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFEFEFEF)),
            contentAlignment = Alignment.Center
        ) {
            Text("Illustration")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onInviteClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
//            Icon(Icons.Default.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Invite a Friend")
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Share your invite",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        InviteOptionsGrid()

        Spacer(modifier = Modifier.height(28.dp))

        RewardCard()

        Spacer(modifier = Modifier.height(28.dp))

        InviteCodeCard(
            inviteCode = inviteCode,
            onCopyCode = onCopyCode
        )

        Spacer(modifier = Modifier.height(28.dp))

        SocialProofSection()
    }
}

@Composable
fun InviteOptionsGrid() {

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

//            InviteOption("Messages", Icons.Default.Message)
//            InviteOption("Copy Link", Icons.Default.Link)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

//            InviteOption("Email", Icons.Default.Email)
//            InviteOption("Contacts", Icons.Default.Contacts)
        }
    }
}

@Composable
fun InviteOption(
    title: String,
    icon: ImageVector
) {

    Card(
        modifier = Modifier
//            .weight(1f)
            .height(80.dp),
        shape = RoundedCornerShape(14.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
//                .clickable { }
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(icon, contentDescription = null)

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun RewardCard() {

    Card(
        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color(0xFFF3F7FF)
//        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "🎁 Train Together Rewards",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("• Beginner challenge")
            Text("• Special badge")
            Text("• Bonus workout")
        }
    }
}

@Composable
fun InviteCodeCard(
    inviteCode: String,
    onCopyCode: () -> Unit
) {

    Card(
        shape = RoundedCornerShape(16.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    text = "Your Invite Code",
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = inviteCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(onClick = onCopyCode) {
                Text("Copy")
            }
        }
    }
}

@Composable
fun SocialProofSection() {

    Column {

        Text(
            text = "Friends make it easier",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(14.dp)
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Alex 🔥 5 day streak",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "\"We finished our first week!\"",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InviteFriendPreview() {
    InviteFriendScreen()
}