package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ID_FILES = "channel_indicadores_novos_arquivos"
    const val CHANNEL_NAME_FILES = "Novas Planilhas e Indicadores"

    const val CHANNEL_ID_AUTH = "channel_indicadores_seguranca"
    const val CHANNEL_NAME_AUTH = "Segurança e Recuperação de Acesso"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val filesChannel = NotificationChannel(
                CHANNEL_ID_FILES,
                CHANNEL_NAME_FILES,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de novas planilhas Excel disponibilizadas pelo ADM"
                enableVibration(true)
            }

            val authChannel = NotificationChannel(
                CHANNEL_ID_AUTH,
                CHANNEL_NAME_AUTH,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de redefinição e envio seguro de senha"
                enableVibration(true)
            }

            manager.createNotificationChannel(filesChannel)
            manager.createNotificationChannel(authChannel)
        }
    }

    fun showNewFileNotification(context: Context, fileName: String, recordCount: Int) {
        initNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_FILES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("📢 Nova Planilha Disponibilizada!")
            .setContentText("O arquivo '$fileName' foi processado em tempo real ($recordCount registros vinculados).")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "O administrador disponibilizou a nova planilha '$fileName'. Seus indicadores técnicos foram recalculados e sincronizados com sucesso."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(101, notification)
        } catch (_: SecurityException) {
            // Permission might be pending or denied in runtime
        }
    }

    fun showPasswordResetNotification(
        context: Context,
        login: String,
        email: String,
        tempPassword: String
    ) {
        initNotificationChannels(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_AUTH)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🔐 Recuperação de Senha - Indicadores Técnicos")
            .setContentText("Nova senha direcionada para o e-mail cadastrado pelo ADM ($email).")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Solicitação de redefinição para o login '$login'.\nNova senha temporária enviada para: $email\nSenha provisória: $tempPassword"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(102, notification)
        } catch (_: SecurityException) {
            // Handled
        }
    }
}
