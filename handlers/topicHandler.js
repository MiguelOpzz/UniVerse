const axios = require('axios');
const { spawn } = require('child_process');

const addTopicsHandler = (db, admin) => async (req, res) => {
  try {
    const { title, description, tags = [], attachmentUrls = [] } = req.body;

    if (!title || !description) {
      return res.status(400).json({
        status: 'fail',
        message: 'Title and description are required.',
      });
    }

    if (!req.user || !req.user.username) {
      return res.status(401).json({
        status: 'fail',
        message: 'User is not authenticated.',
      });
    }

    const createdBy = req.user.username;

    // Combine title, description, and tags for moderation
    const moderationText = `${title} ${description} ${tags.join(' ')}`;

    // Perform moderation check
    let moderationResponse;
    try {
      moderationResponse = await axios.post(
        'https://myproject-441712.et.r.appspot.com/api',
        { text: moderationText }
      );

      if (!moderationResponse.data.is_safe) {
        return res.status(400).json({
          status: 'fail',
          message: 'Content contains offensive language.',
          reason: moderationResponse.data.reason,
        });
      }
    } catch (error) {
      console.error('Moderation API Error:', error.message);
      return res.status(500).json({
        status: 'fail',
        message: 'Error with moderation API.',
        error: error.message,
      });
    }

    // Create new topic data
    const newTopic = {
      title,
      description,
      createdBy,
      tags,
      attachmentUrls,
      postCount: 0,
      likeCount: 0,
      createdAt: admin.firestore.Timestamp.now(),
      updatedAt: admin.firestore.Timestamp.now(),
    };

    // Add new topic to Firestore
    const docRef = await db.collection('topic').add(newTopic);

    // Query Firestore to find the user's document by username
    const userSnapshot = await db
      .collection('users')
      .where('username', '==', createdBy)
      .get();

    if (userSnapshot.empty) {
      return res.status(404).json({
        status: 'fail',
        message: `User document for username '${createdBy}' not found.`,
      });
    }

    // Get the first matching document reference
    const userRef = userSnapshot.docs[0].ref;

    // Increment `TopicCount`
    await userRef.update({
      topicCount: admin.firestore.FieldValue.increment(1),
    });

    return res.status(201).json({
      status: 'success',
      message: 'Topic created successfully.',
      topicId: docRef.id,
    });
  } catch (error) {
    console.error('Error creating topic:', error.message);
    return res.status(500).json({
      status: 'fail',
      message: 'Error creating topic.',
      error: error.message,
    });
  }
};


const getAllTopicsHandler = (db) => async (req, res) => {
  try {
    const snapshot = await db.collection('topic').orderBy('createdAt', 'desc').get();

    if (snapshot.empty) {
      return res.status(404).json({ message: 'No topics found.' });
    }

    const topics = snapshot.docs.map((doc) => ({
      topicId: doc.id,
      ...doc.data(),
    }));

    return res.status(200).json(topics);
  } catch (error) {
    console.error('Error fetching topics:', error.message);
    return res.status(500).json({ message: 'Failed to fetch topics.', error: error.message });
  }
};

const getTopicsByIdHandler = (db) => async (req, res) => {
  try {
    const { topicId } = req.params;
    const doc = await db.collection('topic').doc(topicId).get();

    if (!doc.exists) {
      return res.status(404).json({ message: 'Topic not found.' });
    }

    return res.status(200).json({ topicId: doc.id, ...doc.data() });
  } catch (error) {
    console.error('Error fetching topic:', error.message);
    return res.status(500).json({ message: 'Failed to fetch topic.', error: error.message });
  }
};

const editTopicsByIdHandler = (db, admin) => async (req, res) => {
  try {
    const { topicId } = req.params;
    const { title, description, major, tags } = req.body;

    // Ensure the user is authenticated
    if (!req.user || !req.user.username) {
      return res.status(401).json({
        message: 'User is not authenticated.',
      });
    }

    const username = req.user.username;

    // Retrieve the topic document
    const topicRef = db.collection('topic').doc(topicId);
    const topicDoc = await topicRef.get();

    if (!topicDoc.exists) {
      return res.status(404).json({
        message: 'Topic not found.',
      });
    }

    const topicData = topicDoc.data();

    // Verify that the user is the creator of the topic
    if (topicData.createdBy !== username) {
      return res.status(403).json({
        message: 'You are not authorized to edit this topic.',
      });
    }

    // Filter out undefined values from tags (if provided)
    const sanitizedTags = (tags || topicData.tags || []).filter((tag) => tag !== undefined);

    // Perform moderation check for the updated title, description, and sanitized tags
    const textToModerate = [
      title || topicData.title, // Use existing title if no new title is provided
      description || topicData.description, // Use existing description if no new description is provided
      ...sanitizedTags, // Use sanitized tags
    ].join(' ');

    let moderationResponse;
    try {
      moderationResponse = await axios.post(
        'https://myproject-441712.et.r.appspot.com/api',
        { text: textToModerate }
      );

      if (!moderationResponse.data.is_safe) {
        return res.status(400).json({
          status: 'fail',
          message: 'Content contains offensive language.',
          reason: moderationResponse.data.reason,
        });
      }
    } catch (error) {
      console.error('Moderation API Error:', error.message);
      return res.status(500).json({
        status: 'fail',
        message: 'Error with moderation API.',
        error: error.message,
      });
    }

    // Prepare updated data
    const updatedData = {
      ...(title && { title }),
      ...(description && { description }),
      ...(major && { major }),
      ...(sanitizedTags.length > 0 && { tags: sanitizedTags }), // Only update tags if sanitized tags exist
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    };

    // Update the topic document
    await topicRef.update(updatedData);

    return res.status(200).json({
      message: 'Topic updated successfully!',
    });
  } catch (error) {
    console.error('Error updating topic:', error.message);
    return res.status(500).json({
      message: 'Failed to update topic.',
      error: error.message,
    });
  }
};

const deleteTopicsByIdHandler = (db, admin) => async (req, res) => {
  try {
    const { topicId } = req.params;

    // Ensure the user is authenticated
    if (!req.user || !req.user.username) {
      return res.status(401).json({
        message: 'User is not authenticated.',
      });
    }

    const username = req.user.username;

    // Get the topic document
    const topicDoc = await db.collection('topic').doc(topicId).get();

    if (!topicDoc.exists) {
      return res.status(404).json({ message: 'Topic not found.' });
    }

    const topicData = topicDoc.data();

    // Check if the requesting user is the owner of the topic
    if (topicData.createdBy !== username) {
      return res.status(403).json({ message: 'You are not authorized to delete this topic.' });
    }

    // Delete the topic
    await db.collection('topic').doc(topicId).delete();

    // Decrement topicCount in the user's document
    const userSnapshot = await db.collection('users').where('username', '==', username).get();

    if (!userSnapshot.empty) {
      const userDocRef = userSnapshot.docs[0].ref;
      await userDocRef.update({
        topicCount: admin.firestore.FieldValue.increment(-1),
      });
    }

    return res.status(200).json({ message: 'Topic deleted successfully!' });
  } catch (error) {
    console.error('Error deleting topic:', error.message);
    return res.status(500).json({ message: 'Failed to delete topic.', error: error.message });
  }
};

const recommendTopicsHandler = async (req, res) => {
  try {
    console.log('Request Body:', req.body); // Log the request body to see its contents
    const { forum_title } = req.body;
    if (!forum_title) {
      return res.status(400).json({ message: 'Forum title is required.' });
    }

    // Pre-processing logic (logging the forum title)
    console.log(`Processing recommendation for forum: ${forum_title}`);

    // Sending the forum title to the model (Flask service)
    const modelUrl = 'http://127.0.0.1:8080/api'; // Flask recommendation URL

    const response = await axios.post(modelUrl, { forum_title });

    // Check if the response data exists
    if (!response.data) {
      return res.status(500).json({ message: 'Failed to get recommendations.' });
    }

    // Send the successful response back to the client
    return res.status(200).json(response.data);
  } catch (error) {
    console.error('Error fetching recommendations:', error.message);

    // Handle any errors that might occur
    return res.status(500).json({
      message: 'Internal server error.',
      error: error.message,
    });
  }
};

const toggleLikeTopicHandler = (db, admin) => async (req, res) => {
  try {
    const { topicId } = req.params;

    // Ensure the user is authenticated and has a username
    const username = req.user?.username; // Extracted from token middleware
    if (!username) {
      return res.status(401).json({ message: "Unauthorized. Username is missing." });
    }

    // Get the topic document
    const topicRef = db.collection('topic').doc(topicId);
    const topicDoc = await topicRef.get();

    if (!topicDoc.exists) {
      return res.status(404).json({ message: "Topic not found." });
    }

    const topicData = topicDoc.data();
    const likedBy = topicData.likedBy || [];

    if (likedBy.includes(username)) {
      // User has already liked; perform unlike
      await topicRef.update({
        likedBy: admin.firestore.FieldValue.arrayRemove(username),
        likeCount: admin.firestore.FieldValue.increment(-1),
      });
      return res.status(200).json({ message: "Topic unliked." });
    } else {
      // User has not liked; perform like
      await topicRef.update({
        likedBy: admin.firestore.FieldValue.arrayUnion(username),
        likeCount: admin.firestore.FieldValue.increment(1),
      });
      return res.status(200).json({ message: "Topic liked." });
    }
  } catch (error) {
    console.error("Error toggling like:", error.message);
    return res.status(500).json({
      message: "Failed to toggle like.",
      error: error.message,
    });
  }
};


module.exports = {
  addTopicsHandler,
  getAllTopicsHandler,
  getTopicsByIdHandler,
  editTopicsByIdHandler,
  deleteTopicsByIdHandler,
  toggleLikeTopicHandler,
  recommendTopicsHandler,
};