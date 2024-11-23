const bcrypt = require('bcrypt');

const signUpHandler = (db, admin) => async (req, res) => {
  const { username, email, password, confirmPassword } = req.body;

  if (!username || !email || !password || !confirmPassword) {
    return res.status(400).json({ message: 'All fields are required.' });
  }

  if (password !== confirmPassword) {
    return res.status(400).json({ message: 'Passwords do not match.' });
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

    res.status(201).json({
      message: 'User registered successfully!',
      userId: docRef.id,
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
    // Query Firestore for the user using email or username
    const userSnapshot = await db
      .collection('users')
      .where('email', '==', usernameOrEmail)
      .get();

    // If no user is found using email, search by username
    if (userSnapshot.empty) {
      const usernameSnapshot = await db
        .collection('users')
        .where('username', '==', usernameOrEmail)
        .get();

      if (usernameSnapshot.empty) {
        return res.status(401).json({ message: 'Invalid username/email or password.' });
      }

      // Use the user from the username query
      const user = usernameSnapshot.docs[0].data();

      if (!bcrypt.compareSync(password, user.password)) {
        return res.status(401).json({ message: 'Invalid username/email or password.' });
      }

      return res.status(200).json({ message: `Welcome back, ${user.username}!` });
    }

    // Use the user from the email query
    const user = userSnapshot.docs[0].data();

    if (!bcrypt.compareSync(password, user.password)) {
      return res.status(401).json({ message: 'Invalid username/email or password.' });
    }

    res.status(200).json({ message: `Welcome back, ${user.username}!` });
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

const oauthCallbackHandler = (admin) => async (_req, res) => {
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