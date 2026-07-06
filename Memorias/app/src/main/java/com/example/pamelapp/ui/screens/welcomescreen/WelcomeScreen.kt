package com.example.pamelapp.ui.screens.welcomescreen

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pamelapp.ui.theme.*

@Composable
fun WelcomeScreen(
  navigateToLogin: () -> Unit,
  navigateToRegister: () -> Unit,
  viewModel: WelcomeViewModel = viewModel()
) {
  
  val state by viewModel.state.collectAsState()
  
  LaunchedEffect(state) {
    when (state) {
      WelcomeState.NavigateToLogin -> navigateToLogin()
      WelcomeState.NavigateToRegister -> navigateToRegister()
      else -> Unit
    }
  }
  
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CreamWarm)
      .padding(16.dp), // Padding interno
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = Icons.Default.PhotoCamera,
        contentDescription = "Camara",
        modifier= Modifier.size(80.dp),
        tint = Siena
      )
      
      Text(
        "Memorias",
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
        color = CharcoalWarm,
        textAlign = TextAlign.Center
      )
      
      Text(
        "Revive, cura y atesora tus recuerdos fotográficos",
        fontSize = 16.sp,
        color = BrownMid,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )
      
      Spacer(modifier = Modifier.height(32.dp))
      
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Siena),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = "Calendario",
            modifier= Modifier.size(80.dp),
            tint = CreamWarm
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            "Tus recuerdos te esperan",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )
          Text(
            "Revive momentos especiales",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f)
          )
        }
      }
      
      Spacer(modifier = Modifier.height(48.dp))
      
      Button(
        onClick = { viewModel.onGetStarted() },
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Siena),
        shape = RoundedCornerShape(28.dp)
      ) {
        Text(
          text="Comenzar",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    }
  }
}