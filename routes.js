const express = require('express');
const {
  addCommentHandler,
  getCommentsHandler,
  upvoteCommentHandler,
} = require('./commentHandler');
const {
  addTopicsHandler,
  getAllTopicsHandler,
  getTopicsByIdHandler,
  editTopicsByIdHandler,
  deleteTopicsByIdHandler,
} = require('./topicHandler');
const {
  signUpHandler,
  loginHandler,
  oauthLoginHandler,
  oauthCallbackHandler,
  guestHandler,
} = require('./authHandler');

module.exports = ({ db, admin }) => {
  const router = express.Router();
  router.post('/topics', addTopicsHandler(db, admin));
  router.get('/topics', getAllTopicsHandler(db));
  router.get('/topics/:topicId', getTopicsByIdHandler(db));
  router.put('/topics/:topicId', editTopicsByIdHandler(db, admin));
  router.delete('/topics/:topicId', deleteTopicsByIdHandler(db));

  router.post('/topics/:topicId/comments', addCommentHandler(db, admin))
  router.post('/topics/:topicId/comments/:commentId/upvote', upvoteCommentHandler(db, admin));
  router.get('/topics/:topicId/comments', getCommentsHandler(db));

  router.post('/signup', signUpHandler(db, admin));
  router.post('/guest', guestHandler(db, admin))
  router.post('/login', loginHandler(db));
  router.post('/oauth', oauthLoginHandler(admin));
  router.get('/callback', oauthCallbackHandler(admin));

  return router;
};
