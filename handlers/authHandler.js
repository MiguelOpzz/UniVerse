const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const dotenv = require('dotenv');

dotenv.config();

const generateToken = (user) => {
  return jwt.sign(
    { userId: user.id, username: user.username },
    process.env.JWT_SECRET,  // Use the secret from .env
    { expiresIn: '1h' }
  );
};

const signUpHandler = (db, admin) => async (req, res) => {
  const { username, email, password, confirmPassword } = req.body;

  if (!username || !email || !password ) {
    return res.status(400).json({ message: 'All fields are required.' });
  }
  const userExists = await db.collection('users')
  .where('email', '==', email)
  .get();

  if (!userExists.empty) {
    return res.status(400).json({ message: 'Email already in use.' });
  }

  try {
    const hashedPassword = bcrypt.hashSync(password, 10);

    const newUser = {
      username,
      email,
      password: hashedPassword,
      createdAt: admin.firestore.Timestamp.now(),
    };

    const docRef = await db.collection('users').add(newUser);
    const token = generateToken({ id: docRef.id, username });

    res.status(201).json({
      message: 'User registered successfully!',
      userId: docRef.id,
      token,  // Send the token back to the client
    });

  } catch (error) {
    console.error('Error creating user:', error.message);
    res.status(500).json({
      message: 'Error registering user.',
      error: error.message,
    });
  }
};

const guestHandler = (db, admin) => async (req, res) => {
  try {
    // Generate a random username (improve this for better names)
    const guestUsername = `guest_${Math.random().toString(36).substring(2, 7)}`;

    // Create a temporary user object
    const guestUser = {
      username: guestUsername,
      // Guest users might not have an email
      email: '',
      // No password for guest users
      password: '',
      guest: true, // Flag to identify guest user
      createdAt: admin.firestore.Timestamp.now(),
    };

    // Add guest user to Firestore (modify based on your data structure)
    const docRef = await db.collection('users').add(guestUser);

    res.status(200).json({
      message: 'Welcome as Guest!',
      userId: docRef.id,
      username: guestUsername,
    });
  } catch (error) {
    console.error('Error creating guest user:', error.message);
    res.status(500).json({
      message: 'Error creating guest user.',
      error: error.message,
    });
  }
};

const loginHandler = (db) => async (req, res) => {
  const { usernameOrEmail, password } = req.body;

  if (!usernameOrEmail || !password) {
    return res.status(400).json({ message: 'Username/Email and password are required.' });
  }

  try {
    let userSnapshot = await db
      .collection('users')
      .where('email', '==', usernameOrEmail)
      .get();

    // If no user is found using email, search by username
    if (userSnapshot.empty) {
      userSnapshot = await db
        .collection('users')
        .where('username', '==', usernameOrEmail)
        .get();

      if (userSnapshot.empty) {
        return res.status(401).json({ message: 'Invalid username/email or password.' });
      }
    }

    // Extract the user data and document ID
    const userDoc = userSnapshot.docs[0];
    const user = userDoc.data();

    // Verify the password
    if (!bcrypt.compareSync(password, user.password)) {
      return res.status(401).json({ message: 'Invalid username/email or password.' });
    }

    // Generate a token using the Firestore document ID
    const token = generateToken({ id: userDoc.id, username: user.username });
    res.status(200).json({
      message: `Welcome back, ${user.username}!`,
      token,
    });
  } catch (error) {
    console.error('Error logging in:', error.message);
    res.status(500).json({
      message: 'Error logging in.',
      error: error.message,
    });
  }
};



const oauthLoginHandler = (admin) => async (req, res) => {
  const { provider } = req.body;

  if (!['google', 'apple', 'facebook'].includes(provider)) {
    return res.status(400).json({ message: 'Invalid OAuth provider.' });
  }

  try {
    const redirectUrl = `${req.protocol}://${req.get('host')}/auth/callback`;
    const providerObject = new admin.auth.OAuthProvider(provider);
    providerObject.addScope('profile'); // Request user profile information

    const result = await admin.auth().signInWithRedirect(providerObject, redirectUrl);

    res.status(302).send(); // Temporary redirect to the OAuth provider's login page
  } catch (error) {
    console.error('Error with OAuth login:', error.message);
    res.status(500).json({
      message: 'Error logging in with OAuth.',
      error: error.message,
    });
  }
};

const oauthCallbackHandler = (admin) => async (req, res) => {
  try {
    const credential = await admin.auth().getRedirectResult();

    const user = credential.user;

    const userData = {
      uid: user.uid,
      email: user.email,
      displayName: user.displayName,
    };

    res.status(200).json({
      message: `Welcome back, ${user.displayName}!`,
      user: userData,
    });
  } catch (error) {
    console.error('Error handling OAuth callback:', error.message);
    res.status(401).json({
      message: 'Error handling OAuth callback.',
      error: error.message,
    });
  }
};

module.exports = {
  signUpHandler,
  loginHandler,
  guestHandler,
  oauthLoginHandler,
  oauthCallbackHandler,
};