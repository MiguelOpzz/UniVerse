const addCommentHandler = (db, admin) => async (req, res) => {
  try {
    const { topicId } = req.params; // Get topicId from URL
    const { commentText } = req.body; // Removed userId from body for security
    const { username } = req.user; // Extracted from the token middleware

    if (!commentText) {
      return res.status(400).json({ message: 'Comment text is required.' });
    }

    const newComment = {
      username,  // Use username from token
      commentText,
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

    // Add comment to the 'comments' subcollection
    const commentRef = await topicRef.collection('comments').add(newComment);

    res.status(201).json({
      status: 'success',
      message: 'Comment added successfully',
      commentId: commentRef.id,
      comment: newComment,  // Optionally return the comment data
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
    const { topicId, commentId } = req.params; // Extract from the token middleware
    const { username, id: userId } = req.user; 
    const { voteType } = req.body;

    const validVoteTypes = ['upvote', 'downvote'];
    if (!validVoteTypes.includes(voteType)) {
      return res.status(400).json({ message: 'Invalid vote type.' });
    }

    const commentRef = db.collection('topic').doc(topicId).collection('comments').doc(commentId);
    
    await db.runTransaction(async (transaction) => {
      const commentSnapshot = await transaction.get(commentRef);
      if (!commentSnapshot.exists) throw new Error('Comment not found.');
    
      const commentData = commentSnapshot.data();
      const userVotes = commentData.userVotes || {};
    
      // Determine the new vote state
      if (userVotes[userId] && userVotes[userId].voteType === voteType) {
        delete userVotes[userId];  // Remove the vote if user clicks again
      } else {
        userVotes[userId] = { voteType, username };  // Store voteType and username
      }
    
      // Recalculate upvotes and downvotes
      const updatedUpvotes = Object.values(userVotes).filter(v => v.voteType === 'upvote').length;
      const updatedDownvotes = Object.values(userVotes).filter(v => v.voteType === 'downvote').length;
    
      // Update Firestore document
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


module.exports = {
  addCommentHandler,
  getCommentsHandler,
  upvoteCommentHandler, 
};
