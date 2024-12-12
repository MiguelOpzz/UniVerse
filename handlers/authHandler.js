const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const dotenv = require('dotenv');

dotenv.config();

const generateToken = (user) => {
  return jwt.sign(
    { userId: user.id, username: user.username },
    process.env.JWT_SECRET,  // Use the secret from .env
  );
};

const signUpHandler = (db, admin) => async (req, res) => {
  const { username, email, password, university, profilePicture } = req.body;

  if (!username || !email || !password) {
    return res.status(400).json({ message: 'Username, email, and password are required.' });
  }

  // Make sure the university and profilePicture fields are not provided during signup
  if (university || profilePicture) {
    return res.status(400).json({ message: 'University and Profile Picture can only be set through the edit user endpoint.' });
  }

  try {
    // Check if the username is already taken
    const usernameDoc = await db.collection('users').doc(username).get();
    if (usernameDoc.exists) {
      return res.status(400).json({ message: 'Username already in use.' });
    }

    // Check if the email is already in use
    const emailExists = await db
      .collection('users')
      .where('email', '==', email)
      .get();

    if (!emailExists.empty) {
      return res.status(400).json({ message: 'Email already in use.' });
    }

    // Hash the password
    const hashedPassword = bcrypt.hashSync(password, 10);

    // Prepare the user object
    const newUser = {
      username,
      email,
      password: hashedPassword,
      topicCount: 0,
      commentCount: 0,
      createdAt: admin.firestore.Timestamp.now(),
      // Initially, set university and profilePicture to null or undefined
      university: null,
      profilePicture: null,
    };

    // Set the document with the username as the ID
    await db.collection('users').doc(username).set(newUser);

    res.status(201).json({
      message: 'User registered successfully!',
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
      username: user.username,
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

const editUserHandler = (db) => async (req, res) => {
  const { username } = req.params; // Assuming the username is passed in the URL as a parameter
  const { university, profilePicture } = req.body;

  // Extract the token from the headers
  const token = req.headers.authorization?.split(' ')[1];
  if (!token) {
    return res.status(401).json({ message: 'Authorization token is required.' });
  }

  try {
    // Verify the token and extract the payload
    const decodedToken = jwt.verify(token, process.env.JWT_SECRET);
    const tokenUsername = decodedToken.username;

    // Check if the token's username matches the username in the request
    if (tokenUsername !== username) {
      return res.status(403).json({ message: 'You are not authorized to edit this user profile.' });
    }

    // Check if at least one field is provided
    if (!university && !profilePicture) {
      return res.status(400).json({ message: 'At least one field (university or profile picture) must be provided.' });
    }

    // Get the current user's document
    const userDoc = await db.collection('users').doc(username).get();
    if (!userDoc.exists) {
      return res.status(404).json({ message: 'User not found.' });
    }

    // Prepare the update object, only including fields that were provided
    const updateData = {};
    if (university) updateData.university = university;
    if (profilePicture) updateData.profilePicture = profilePicture;

    // Update the user's document
    await db.collection('users').doc(username).update(updateData);

    res.status(200).json({
      message: 'User information updated successfully.',
      updatedFields: updateData,
    });
  } catch (error) {
    if (error.name === 'JsonWebTokenError') {
      return res.status(401).json({ message: 'Invalid token.' });
    }

    console.error('Error updating user information:', error.message);
    res.status(500).json({
      message: 'Error updating user information.',
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
  editUserHandler
};
