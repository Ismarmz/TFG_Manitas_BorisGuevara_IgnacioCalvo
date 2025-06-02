const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const logger = require("firebase-functions/logger");

initializeApp();
const db = getFirestore();

exports.sendChatNotification = onDocumentCreated("chats/{chatId}/messages/{messageId}", async (event) => {
    const message = event.data.data();
    const senderId = message.senderId;
    const chatId = event.params.chatId;

    const chatDoc = await db.collection("chats").doc(chatId).get();
    const chatData = chatDoc.data();
    const userIds = chatData.userIds;

    const recipientId = userIds.find(uid => uid !== senderId);
    if (!recipientId) return;

    const userDoc = await db.collection("users").doc(recipientId).get();
    const token = userDoc.data()?.fcmToken;

    if (!token) {
        logger.info("No hay token para el usuario", recipientId);
        return;
    }

    // Obtener nombre del remitente
    let senderName = "Alguien";
    try {
        const senderDoc = await db.collection("users").doc(senderId).get();
        senderName = senderDoc.data()?.name || "Alguien";
    } catch (e) {
        logger.warn("No se pudo obtener el nombre del remitente:", e);
    }

    const isImage = message.type === "image";

const payload = {
    data: {
        title: `${senderName} te envió un mensaje`,
        body: isImage ? "📷 Imagen enviada" : message.text || "Nuevo mensaje",
        chatId: chatId,
        jobId: chatData.jobId,
        otherUserId: senderId, // El emisor es el otro usuario desde la perspectiva del receptor
        ...(isImage && { imageUrl: message.text })
    },
    token: token
};


    try {
        const response = await getMessaging().send(payload);
        logger.info("Notificación enviada", response);
    } catch (error) {
        logger.error("Error enviando notificación", error);
    }
});
