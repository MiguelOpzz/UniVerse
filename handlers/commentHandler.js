//const { predictSentiment } = require('./predictSentiment.js');

const addCommentHandler = (db, admin) => async (req, res) => {
  try {
    const { topicId } = req.params;
    const { commentText } = req.body;

    if (!req.user || !req.user.username) {
      return res.status(401).json({
        message: 'User is not authenticated.',
      });
    }

    const username = req.user.username;

    if (!commentText) {
      return res.status(400).json({ message: 'Comment text is required.' });
    }

    // Step 1: Predict sentiment of the comment
    //const sentiment = await predictSentiment(commentText);  // Predict sentiment here

    // Step 2: Store the sentiment alongside the comment in Firestore
    const newComment = {
      username,
      commentText,
      //sentiment,  // Store sentiment prediction (e.g., 'positive', 'negative')
      createdBy: username,
      upvotes: 0,
      downvotes: 0,
      userVotes: {},
      createdAt: admin.firestore.Timestamp.now(),
      updatedAt: admin.firestore.Timestamp.now(),
    };

    const topicRef = db.collection('topic').doc(topicId);
    const topicSnapshot = await topicRef.get();

    if (!topicSnapshot.exists) {
      return res.status(404).json({ message: 'Topic not found.' });
    }

    const commentRef = await topicRef.collection('comments').add(newComment);

    const userRef = db.collection('users').doc(username);
    const userSnapshot = await userRef.get();

    if (!userSnapshot.exists) {
      // Create the user document if it doesn't exist
      await userRef.set({ username, commentCount: 0, topicCount: 0 });
    }

    await userRef.update({
      commentCount: admin.firestore.FieldValue.increment(1),
    });

    res.status(201).json({
      status: 'success',
      message: 'Comment added successfully',
      commentId: commentRef.id,
      comment: newComment,
    });
  } catch (error) {
    console.error('Error adding comment:', error.message);
    res.status(500).json({ status: 'fail', message: 'Error adding comment', error: error.message });
  }
};

const getCommentsHandler = (db) => async (req, res) => {
  try {
    const { topicId } = req.params;

    // Verify if the topic exists
    const topicRef = db.collection('topic').doc(topicId);
    const topicDoc = await topicRef.get();

    if (!topicDoc.exists) {
      return res.status(404).json({ message: 'Topic not found.' });
    }

    // Retrieve comments for the topic
    const snapshot = await topicRef
      .collection('comments')
      .orderBy('createdAt', 'desc')
      .get();

    // Handle case where no comments are found
    if (snapshot.empty) {
      return res.status(200).json([]); // Return empty array if no comments
    }

    // Map comment data
    const comments = snapshot.docs.map((doc) => ({
      commentId: doc.id,
      ...doc.data(),
    }));

    return res.status(200).json(comments);
  } catch (error) {
    console.error('Error fetching comments:', error.message);
    return res.status(500).json({
      message: 'Failed to fetch comments.',
      error: error.message,
    });
  }
};

const getCommentByIdHandler = (db) => async (req, res) => {
  try {
    const { topicId, commentId } = req.params;

    // Verify if the topic exists
    const topicRef = db.collection('topic').doc(topicId);
    const topicDoc = await topicRef.get();

    if (!topicDoc.exists) {
      return res.status(404).json({ message: 'Topic not found.' });
    }

    // Get the comment document
    const commentRef = topicRef.collection('comments').doc(commentId);
    const commentDoc = await commentRef.get();

    if (!commentDoc.exists) {
      return res.status(404).json({ message: 'Comment not found.' });
    }

    // Respond with the comment data
    return res.status(200).json({
      commentId: commentDoc.id,
      ...commentDoc.data(),
    });
  } catch (error) {
    console.error('Error fetching comment:', error.message);
    return res.status(500).json({
      message: 'Failed to fetch comment.',
      error: error.message,
    });
  }
};

const upvoteCommentHandler = (db, admin) => async (req, res) => {
  try {
    const { topicId, commentId } = req.params;
    const { voteType } = req.body;

    // Ensure the user is authenticated and has a username in the token
    if (!req.user || !req.user.username) {
      return res.status(401).json({ message: 'User is not authenticated.' });
    }

    const username = req.user.username; // Extract username from token
    const validVoteTypes = ['upvote', 'downvote'];

    if (!validVoteTypes.includes(voteType)) {
      return res.status(400).json({ message: 'Invalid vote type.' });
    }

    const commentRef = db.collection('topic').doc(topicId).collection('comments').doc(commentId);

    await db.runTransaction(async (transaction) => {
      const commentSnapshot = await transaction.get(commentRef);

      if (!commentSnapshot.exists) {
        throw new Error('Comment not found.');
      }

      const commentData = commentSnapshot.data();
      const userVotes = commentData.userVotes || {};

      // Handle user vote state
      if (userVotes[username] && userVotes[username].voteType === voteType) {
        // If the user has already voted the same way, remove their vote
        delete userVotes[username];
      } else {
        // Otherwise, add or update the user's vote
        userVotes[username] = { voteType };
      }

      // Calculate the new upvote and downvote counts
      const updatedUpvotes = Object.values(userVotes).filter((vote) => vote.voteType === 'upvote').length;
      const updatedDownvotes = Object.values(userVotes).filter((vote) => vote.voteType === 'downvote').length;

      // Update the comment document in Firestore
      transaction.update(commentRef, {
        upvotes: updatedUpvotes,
        downvotes: updatedDownvotes,
        userVotes,
        updatedAt: admin.firestore.Timestamp.now(),
      });
    });

    res.status(200).json({ message: 'Vote updated successfully!' });
  } catch (error) {
    console.error('Error voting on comment:', error.message);
    res.status(500).json({ message: 'Failed to vote on comment.', error: error.message });
  }
};

const editCommentHandler = (db, admin) => async (req, res) => {
  try {
    const { topicId, commentId } = req.params;
    const { commentText } = req.body;
    const { username } = req.user; // Extracted from token

    if (!commentText) {
      return res.status(400).json({ message: 'Comment text is required.' });
    }

    const topicRef = db.collection('topic').doc(topicId);
    const commentRef = topicRef.collection('comments').doc(commentId);
    const commentSnapshot = await commentRef.get();

    if (!commentSnapshot.exists) {
      return res.status(404).json({ message: 'Comment not found.' });
    }

    const commentData = commentSnapshot.data();

    // Check if the requesting user is the owner of the comment
    if (commentData.username !== username) {
      return res.status(403).json({ message: 'You can only edit your own comment.' });
    }

    // Update the comment
    await commentRef.update({
      commentText,
      updatedAt: admin.firestore.Timestamp.now(),
    });

    res.status(200).json({
      status: 'success',
      message: 'Comment updated successfully!',
    });
  } catch (error) {
    console.error('Error updating comment:', error.message);
    res.status(500).json({ message: 'Failed to update comment.', error: error.message });
  }
};

const deleteCommentHandler = (db, admin) => async (req, res) => {
  try {
    const { topicId, commentId } = req.params;
    const { username } = req.user; // Extracted from token

    // Verify the comment exists
    const commentRef = db.collection('topic').doc(topicId).collection('comments').doc(commentId);
    const commentDoc = await commentRef.get();

    if (!commentDoc.exists) {
      return res.status(404).json({ message: 'Comment not found.' });
    }

    const commentData = commentDoc.data();

    // Verify ownership
    if (commentData.createdBy !== username) {
      return res.status(403).json({ message: 'Unauthorized to delete this comment.' });
    }

    // Start Firestore transaction for deleting the comment
    await db.runTransaction(async (transaction) => {
      const commentSnapshot = await transaction.get(commentRef);

      if (!commentSnapshot.exists) {
        throw new Error('Comment not found.');
      }

      // Delete the comment in the transaction
      transaction.delete(commentRef);
    });

    // Now update the user's comment count outside of the transaction
    const userRef = db.collection('users').doc(username);
    await userRef.update({
      commentCount: admin.firestore.FieldValue.increment(-1),
    });

    res.status(200).json({ message: 'Comment deleted successfully!' });
  } catch (error) {
    console.error('Error deleting comment:', error.message);
    res.status(500).json({ message: 'Failed to delete comment.', error: error.message });
  }
};

module.exports = {
  addCommentHandler,
  getCommentsHandler,
  getCommentByIdHandler,
  upvoteCommentHandler, 
  deleteCommentHandler,
  editCommentHandler,
};