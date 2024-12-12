const express = require('express');
const {
  addCommentHandler,
  getCommentsHandler,
  getCommentByIdHandler,
  upvoteCommentHandler,
  deleteCommentHandler,
  editCommentHandler,
} = require('./handlers/commentHandler');
const {
  addTopicsHandler,
  getAllTopicsHandler,
  getTopicsByIdHandler,
  editTopicsByIdHandler,
  deleteTopicsByIdHandler,
  toggleLikeTopicHandler,
  recommendTopicsHandler,
  processDescriptionHandler,
  getProcessedDescriptionHandler,
} = require('./handlers/topicHandler');
const {
  signUpHandler,
  loginHandler,
  oauthLoginHandler,
  oauthCallbackHandler,
  guestHandler,
  editUserHandler,
} = require('./handlers/authHandler');
const { authenticateToken } = require('./middleware/authmiddleware'); // Import the middleware

module.exports = ({ db, admin }) => {
  const router = express.Router();
  router.post('/topics', authenticateToken, addTopicsHandler(db, admin));
  router.get('/topics', getAllTopicsHandler(db));
  router.get('/topics/:topicId', getTopicsByIdHandler(db));
  router.put('/topics/:topicId', authenticateToken, editTopicsByIdHandler(db, admin));
  router.delete('/topics/:topicId', authenticateToken, deleteTopicsByIdHandler(db, admin));
  router.post('/topics/:topicId/like',authenticateToken ,toggleLikeTopicHandler (db, admin));
  router.get('/topics/:topicId/recommend',recommendTopicsHandler (db));
  router.post('/topics/:topicId/process-description', processDescriptionHandler (db, admin));
  router.get('/topics/:topicId/process-description', getProcessedDescriptionHandler (db, admin))

  router.post('/topics/:topicId/comments', authenticateToken, addCommentHandler(db, admin));
  router.post('/topics/:topicId/comments/:commentId/upvote',authenticateToken, upvoteCommentHandler(db, admin));
  router.post('/topics/:topicId/comments/:commentId',authenticateToken, editCommentHandler(db, admin));
  router.delete('/topics/:topicId/comments/:commentId', authenticateToken, deleteCommentHandler(db, admin));
  router.get('/topics/:topicId/comments', getCommentsHandler(db));
  router.get('/topics/:topicId/comments/:commentId', getCommentByIdHandler (db));

  router.post('/signup', signUpHandler(db, admin));
  router.post('/guest', guestHandler(db, admin))
  router.post('/login', loginHandler(db));
  router.post('/oauth', oauthLoginHandler(admin));
  router.get('/callback', oauthCallbackHandler(admin));
  router.put('/users/:username/edit', editUserHandler(db));

  return router;
};
