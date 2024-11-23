/* eslint-disable linebreak-style */
const express = require('express');
const admin = require('firebase-admin');
const routes = require('./routes');
const credentials = require('./key.json');

// Initialize Firebase Admin SDK
admin.initializeApp({
  credential: admin.credential.cert(credentials),
});

const db = admin.firestore(); // Firestore database instance
const app = express();

// Middleware for JSON and URL-encoded payloads
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Mount routes (pass `db` and `admin`)
app.use('/api', routes({ db, admin }));

// Start server
const PORT = process.env.PORT || 8000;
app.listen(PORT, () => {
  console.log(`Server is running on PORT ${PORT}.`);
});
