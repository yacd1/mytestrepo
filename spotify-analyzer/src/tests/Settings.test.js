jest.mock('../services/api');

import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { ThemeProvider } from '../services/Theme';
import Settings from '../components/Settings';
import '@testing-library/jest-dom';
import userEvent from '@testing-library/user-event';
import { act } from 'react';
import { apiService } from '../services/api';

beforeAll(() => {
    window.matchMedia = window.matchMedia || function () {
        return {
            matches: false,
            addListener: () => {},
            removeListener: () => {},
        };
    };
});

beforeEach(() => {
    apiService.checkSpotifyStatus.mockResolvedValue({
        authenticated: false,
    });
    apiService.getUserArtistMinigameTime.mockResolvedValue({ artistMinigameTime: 100 });
    apiService.getUserTrackMinigameTime.mockResolvedValue({ trackMinigameTime: 200 });
    apiService.getTopMinigamePlayers.mockResolvedValue([
      { spotifyUsername: 'u1', minigameBestTimeInSeconds: 10 }
    ]);
    apiService.deleteArtistMinigameScore.mockResolvedValue({ success: true });
    apiService.deleteTrackMinigameScore.mockResolvedValue({ success: true });
    apiService.deleteBothMinigameScores.mockResolvedValue({ success: true });
});

describe('Settings Component', () => {
    it('renders the page without errors', () => {
        render(
            <ThemeProvider>
                <Settings />
            </ThemeProvider>
        );
    });

    it('toggles dark mode on and off', async () => {
        render(
            <ThemeProvider>
                <Settings />
            </ThemeProvider>
        );

        const darkModeSwitch = screen.getByRole('checkbox');
        
        expect(darkModeSwitch).not.toBeChecked();

        await act(async () => {
            userEvent.click(darkModeSwitch);
        });

        expect(darkModeSwitch).toBeChecked();

        await act(async () => {
            userEvent.click(darkModeSwitch);
        });

        expect(darkModeSwitch).not.toBeChecked();
    });

    it('displays user profile and high scores when logged in', async () => {
        const mockProfile = { display_name: 'Test User' };
        const mockArtistHighScore = 100;
        const mockTrackHighScore = 200;

        apiService.checkSpotifyStatus.mockResolvedValue({ authenticated: true, profile: mockProfile });
        apiService.getUserArtistMinigameTime.mockResolvedValue({ artistMinigameTime: mockArtistHighScore });
        apiService.getUserTrackMinigameTime.mockResolvedValue({ trackMinigameTime: mockTrackHighScore });

        render(
            <ThemeProvider>
                <Settings />
            </ThemeProvider>
        );

        await waitFor(() => screen.getByText(/Logged in as: Test User/));

        expect(screen.getByText(/Logged in as: Test User/)).toBeInTheDocument();
        expect(screen.getByText(/Artist Game Best Time: 100 seconds/)).toBeInTheDocument();
        expect(screen.getByText(/Track Game Best Time: 200 seconds/)).toBeInTheDocument();
    });

    it('shows login prompt when not logged in', async () => {
        apiService.checkSpotifyStatus.mockResolvedValue({ authenticated: false });

        render(
            <ThemeProvider>
                <Settings />
            </ThemeProvider>
        );

        expect(screen.getByText(/Login with Spotify to manage your minigame scores/)).toBeInTheDocument();
    });

    it('handles artist score deletion process', async () => {
        const mockProfile = { display_name: 'Test User' };
        const mockArtistHighScore = 100;

        apiService.checkSpotifyStatus.mockResolvedValue({ authenticated: true, profile: mockProfile });
        apiService.getUserArtistMinigameTime.mockResolvedValue({ artistMinigameTime: mockArtistHighScore });
        apiService.deleteArtistMinigameScore.mockResolvedValue({ success: true });

        render(
            <ThemeProvider>
                <Settings />
            </ThemeProvider>
        );

        await waitFor(() => screen.getByText(/Logged in as: Test User/));

        const deleteButton = screen.getByRole('button', { name: /Reset Artist Score/i });

        await act(async () => {
            userEvent.click(deleteButton);
        });

        expect(apiService.deleteArtistMinigameScore).toHaveBeenCalledWith(mockProfile.display_name);
        expect(screen.getByText(/Artist game high score deleted successfully!/)).toBeInTheDocument();
    });

    it('handles track score deletion process', async () => {
        const mockProfile = { display_name: 'Test User' };
        const mockTrackHighScore = 200;

        apiService.checkSpotifyStatus.mockResolvedValue({ authenticated: true, profile: mockProfile });
        apiService.getUserTrackMinigameTime.mockResolvedValue({ trackMinigameTime: mockTrackHighScore });
        apiService.deleteTrackMinigameScore.mockResolvedValue({ success: true });

        render(
            <ThemeProvider>
                <Settings />
            </ThemeProvider>
        );

        await waitFor(() => screen.getByText(/Logged in as: Test User/));

        const deleteButton = screen.getByRole('button', { name: /Reset Track Score/i });

        await act(async () => {
            userEvent.click(deleteButton);
        });

        expect(apiService.deleteTrackMinigameScore).toHaveBeenCalledWith(mockProfile.display_name);
        expect(screen.getByText(/Track game high score deleted successfully!/)).toBeInTheDocument();
    });

    it('handles both artist and track score deletion process', async () => {
        const mockProfile = { display_name: 'Test User' };
        const mockArtistHighScore = 100;
        const mockTrackHighScore = 200;

        apiService.checkSpotifyStatus.mockResolvedValue({ authenticated: true, profile: mockProfile });
        apiService.getUserArtistMinigameTime.mockResolvedValue({ artistMinigameTime: mockArtistHighScore });
        apiService.getUserTrackMinigameTime.mockResolvedValue({ trackMinigameTime: mockTrackHighScore });
        apiService.deleteBothMinigameScores.mockResolvedValue({ success: true });

        render(
            <ThemeProvider>
                <Settings />
            </ThemeProvider>
        );

        await waitFor(() => screen.getByText(/Logged in as: Test User/));

        const deleteButton = screen.getByRole('button', { name: /Reset All Scores/i });

        await act(async () => {
            userEvent.click(deleteButton);
        });

        expect(apiService.deleteBothMinigameScores).toHaveBeenCalledWith(mockProfile.display_name);
        expect(screen.getByText(/All high scores deleted successfully!/)).toBeInTheDocument();
    });
});
