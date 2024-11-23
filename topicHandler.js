const addTopicsHandler = (db, admin) => async (req, res) => {
  try {
    const {
      title,
      description,
      createdBy,
      tags,
      isNSFW,
      attachmentUrls = [],
    } = req.body;

    const newTopic = {
      title,
      description: description || null,
      createdBy,
      tags: tags || [],
      isNSFW,
      attachmentUrls,
      postCount: 0,
      createdAt: admin.firestore.Timestamp.now(),
      updatedAt: admin.firestore.Timestamp.now(),
    };

    const docRef = await db.collection('topic').add(newTopic);
    res.status(201).json({
      status: 'success',
      message: 'Topic created successfully',
      topicId: docRef.id,
    });
  } catch (error) {
    console.error('Error creating topic:', error.message);
    res.status(500).json({
      status: 'fail',
      message: 'Error creating topic',
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
    const { title, description, major, tags, isNSFW } = req.body;

    const updatedData = {
      ...(title && { title }),
      ...(description && { description }),
      ...(major && { major }),
      ...(tags && { tags }),
      ...(isNSFW !== undefined && { isNSFW }),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    };

    await db.collection('topic').doc(topicId).update(updatedData);
    return res.status(200).json({ message: 'Topic updated successfully!' });
  } catch (error) {
    console.error('Error updating topic:', error.message);
    return res.status(500).json({ message: 'Failed to update topic.', error: error.message });
  }
};

const deleteTopicsByIdHandler = (db) => async (req, res) => {
  try {
    const { topicId } = req.params;

    await db.collection('topic').doc(topicId).delete();
    return res.status(200).json({ message: 'Topic deleted successfully!' });
  } catch (error) {
    console.error('Error deleting topic:', error.message);
    return res.status(500).json({ message: 'Failed to delete topic.', error: error.message });
  }
};

module.exports = {
  addTopicsHandler,
  getAllTopicsHandler,
  getTopicsByIdHandler,
  editTopicsByIdHandler,
  deleteTopicsByIdHandler,
};
