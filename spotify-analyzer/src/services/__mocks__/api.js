module.exports = {
  __esModule: true,
  apiService: {
    checkSpotifyStatus: jest.fn().mockResolvedValue({
      profile: { display_name: "Mock User", id: "mock_user_id" },
      artistBestTime: 42,
      trackBestTime: 99,
      topPlayers: [{ spotifyUsername: "u1", minigameBestTimeInSeconds: 10 }],
    }),
    getUserArtistMinigameTime: jest.fn().mockResolvedValue({ artistMinigameTime: 100 }),
    getUserTrackMinigameTime: jest.fn().mockResolvedValue({ trackMinigameTime: 200 }),
    getTopMinigamePlayers: jest.fn().mockResolvedValue([{ spotifyUsername: "u1", minigameBestTimeInSeconds: 10 }]),
    deleteArtistMinigameScore: jest.fn(),
    deleteTrackMinigameScore: jest.fn(),
    deleteBothMinigameScores: jest.fn(),
  }
};