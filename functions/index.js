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

    const payload = {
        notification: {
            title: "Nuevo mensaje",
            body: message.text || "Has recibido un mensaje",
        },
        token: token,
    };

    try {
        const response = await getMessaging().send(payload);
        logger.info("Notificación enviada", response);
    } catch (error) {
        logger.error("Error enviando notificación", error);
    }
});
