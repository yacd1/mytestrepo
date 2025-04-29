jest.mock('../services/api')
jest.mock('../services/minigameHandler');

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';

import { apiService } from '../services/api';
import {
  fetchTopArtists,
  searchArtists,
  fetchTopTracks,
  searchTracks
} from '../services/minigameHandler';

import Minigames from '../components/Minigames';
import { ThemeProvider } from '../services/Theme';

const mockMatchMedia = () => ({
  matches: true,
  media: '(prefers-color-scheme: dark)',
  onchange: null,
  addListener: jest.fn(),
  removeListener: jest.fn(),
  addEventListener: jest.fn(),
  removeEventListener: jest.fn(),
  dispatchEvent: jest.fn(),
});

beforeEach(() => {
  apiService.checkSpotifyStatus.mockResolvedValue({
    profile: { display_name: 'Mock User', id: 'mock_user_id' },
    artistBestTime: 42,
    trackBestTime: 99,
    topPlayers: [{ spotifyUsername: 'u1', minigameBestTimeInSeconds: 10 }],
  });
  apiService.getUserArtistMinigameTime.mockResolvedValue({ artistMinigameTime: 100 });
  apiService.getUserTrackMinigameTime.mockResolvedValue({ trackMinigameTime: 200 });
  apiService.getTopMinigamePlayers.mockResolvedValue([
    { spotifyUsername: 'u1', minigameBestTimeInSeconds: 10 }
  ]);

  fetchTopArtists.mockResolvedValue(["Artist 1", "Artist 2", "Artist 3"])
  searchArtists.mockResolvedValue(["Artist 1", "Artist 2", "Artist 3"])
  fetchTopTracks.mockResolvedValue(["Track 1 by A", "Track 2 by B", "Track 3 by C"])
  searchTracks.mockResolvedValue(["Track 1 by A", "Track 2 by B", "Track 3 by C"])
});

describe('Minigames Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: jest.fn().mockImplementation(mockMatchMedia),
    });
  });

  it('renders with artist mode initially', async () => {
    render(
      <ThemeProvider>
        <Minigames />
      </ThemeProvider>
    );
    await waitFor(() => {
      expect(screen.getByText(/Guess Artists Rankings/i)).toBeInTheDocument();
      expect(fetchTopArtists).toHaveBeenCalled();
    });
  });

  it('toggles mode between artists and tracks', async () => {
    render(
      <ThemeProvider>
        <Minigames />
      </ThemeProvider>
    );
    await waitFor(() => screen.getByText(/Guess Artists Rankings/i));
    fireEvent.click(screen.getByRole('button', { name: /Tracks/i }));
    await waitFor(() => {
      expect(screen.getByText(/Guess Tracks Rankings/i)).toBeInTheDocument();
      expect(fetchTopTracks).toHaveBeenCalled();
    });
  });

  it('starts timer when first guess is made', async () => {
    render(
      <ThemeProvider>
        <Minigames />
      </ThemeProvider>
    );
    await waitFor(() => screen.getByPlaceholderText(/Enter artist name/i));
    fireEvent.change(screen.getByPlaceholderText(/Enter artist name/i), {
      target: { value: 'Artist 1' }
    });
    fireEvent.click(screen.getByRole('button', { name: /^Guess$/i }));
    await waitFor(() => {
      expect(screen.getByText(/Time: \d+s/)).toBeInTheDocument();
    });
  });  
});
