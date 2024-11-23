const addCommentHandler = (db, admin) => async (req, res) => {
  try {
    const { topicId } = req.params; // Get topicId from URL
    const { userId, commentText } = req.body;

    if (!userId || !commentText) {
      return res.status(400).json({ message: 'UserId and commentText are required.' });
    }

    const newComment = {
      userId,
      commentText,
      upvotes: 0,
      downvotes: 0,
      userVotes: {},
      createdAt: admin.firestore.Timestamp.now(),
      updatedAt: admin.firestore.Timestamp.now(),
    };

    const topicRef = db.collection('topic').doc(topicId);
    const topic = await topicRef.get();

    if (!topic.exists) {
      return res.status(404).json({ message: 'Topic not found.' });
    }

    // Add comment to a subcollection
    const commentRef = await topicRef.collection('comments').add(newComment);
    res.status(201).json({
      status: 'success',
      message: 'Comment added successfully',
      commentId: commentRef.id,
    });
  } catch (error) {
    console.error('Error adding comment:', error.message);
    res.status(500).json({ status: 'fail', message: 'Error adding comment', error: error.message });
  }
};

const getCommentsHandler = (db) => async (req, res) => {
  try {
    const { topicId } = req.params;

    const topicRef = db.collection('topic').doc(topicId);
    const topic = await topicRef.get();

    if (!topic.exists) {
      return res.status(404).json({ message: 'Topic not found.' });
    }

    const snapshot = await topicRef.collection('comments').orderBy('createdAt', 'desc').get();

    if (snapshot.empty) {
      return res.status(404).json({ message: 'No comments found.' });
    }

    const comments = snapshot.docs.map((doc) => ({
      commentId: doc.id,
      ...doc.data(),
    }));

    res.status(200).json(comments);
  } catch (error) {
    console.error('Error fetching comments:', error.message);
    res.status(500).json({ message: 'Failed to fetch comments.', error: error.message });
  }
};

const upvoteCommentHandler = (db, admin) => async (req, res) => {
  try {
    const { topicId, commentId } = req.params;
    const { userId, voteType } = req.body;  // voteType: 'upvote' or 'downvote'

    if (!userId || !['upvote', 'downvote'].includes(voteType)) {
      return res.status(400).json({ message: 'Invalid vote request.' });
    }

    const commentRef = db.collection('topic').doc(topicId).collection('comments').doc(commentId);
    const commentSnapshot = await commentRef.get();

    if (!commentSnapshot.exists) {
      return res.status(404).json({ message: 'Comment not found.' });
    }

    const commentData = commentSnapshot.data();

    // Ensure userVotes exists and is an object
    const userVotes = commentData.userVotes || {};

    const previousVote = userVotes[userId];

    // Update vote counts based on previous and current votes
    if (previousVote === voteType) {
      // Remove the vote if the user clicks the same vote again
      if (voteType === 'upvote') commentData.upvotes--;
      else commentData.downvotes--;
      delete userVotes[userId];
    } else {
      // Adjust vote counts
      if (previousVote === 'upvote') commentData.upvotes--;
      if (previousVote === 'downvote') commentData.downvotes--;

      if (voteType === 'upvote') commentData.upvotes++;
      else commentData.downvotes++;

      userVotes[userId] = voteType;
    }

    // Update the Firestore document
    await commentRef.update({
      upvotes: commentData.upvotes,
      downvotes: commentData.downvotes,
      userVotes: userVotes,
      updatedAt: admin.firestore.Timestamp.now(),
    });

    res.status(200).json({ 
      message: 'Vote updated successfully!', 
      upvotes: commentData.upvotes, 
      downvotes: commentData.downvotes 
    });
  } catch (error) {
    console.error('Error voting on comment:', error.message);
    res.status(500).json({ message: 'Failed to vote on comment.', error: error.message });
  }
};

module.exports = {
  addCommentHandler,
  getCommentsHandler,
  upvoteCommentHandler, 
};
