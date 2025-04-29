module.exports = {
  __esModule: true,
  fetchTopArtists: jest.fn().mockResolvedValue(["Artist 1", "Artist 2", "Artist 3"]),
  searchArtists:    jest.fn().mockResolvedValue(["Artist 1", "Artist 2", "Artist 3"]),
  fetchTopTracks:   jest.fn().mockResolvedValue(["Track 1 by A", "Track 2 by B", "Track 3 by C"]),
  searchTracks:     jest.fn().mockResolvedValue(["Track 1 by A", "Track 2 by B", "Track 3 by C"]),
};
