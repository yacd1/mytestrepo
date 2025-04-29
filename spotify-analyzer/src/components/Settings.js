import React, { useContext, useState, useEffect } from 'react';
import { Theme } from '../services/Theme';
import { apiService } from '../services/api';
import '../styles/Settings.css';
import '../styles/App.css';

function Settings() {
    const { isDarkMode, toggleTheme } = useContext(Theme);
    const [userProfile, setUserProfile] = useState(null);
    const [artistGameHighScore, setArtistGameHighScore] = useState(null);
    const [trackGameHighScore, setTrackGameHighScore] = useState(null);
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const fetchUserData = async () => {
            try {
                const status = await apiService.checkSpotifyStatus();
                if (status.authenticated && status.profile) {
                    setUserProfile(status.profile);

                    try {
                        const artistScoreResponse = await apiService.getUserArtistMinigameTime(status.profile.display_name);
                        if (artistScoreResponse.artistMinigameTime !== false) {
                            setArtistGameHighScore(artistScoreResponse.artistMinigameTime);
                            setArtistGameHighScore(artistScoreResponse.artistMinigameTime);
                        }
                        else {
                            console.log("No artist score found");
                            setArtistGameHighScore(null);
                        }

                        const trackScoreResponse = await apiService.getUserTrackMinigameTime(status.profile.display_name);
                        if (trackScoreResponse.trackMinigameTime !== false) {
                            setTrackGameHighScore(trackScoreResponse.trackMinigameTime);
                        }
                        else {
                            console.log("No track score found");
                            setTrackGameHighScore(null);
                        }
                    } catch (error) {
                        console.log("No high scores found for user");
                        setArtistGameHighScore(null);
                        setTrackGameHighScore(null);
                    }
                }
            } catch (error) {
                console.error("Error fetching user profile:", error);
            }
        };

        fetchUserData();
    }, []);

    const handleRemoveHighScore = async (scoreType) => {
        if (!userProfile) {
            setMessage('Please log in to manage your high scores');
            return;
        }

        setLoading(true);
        try {
            // Check if the user exists and has the relevant score
            if (scoreType === 'artist') {
                    await apiService.deleteArtistMinigameScore(userProfile.display_name);
                    setMessage('Artist game high score deleted successfully!');
                    setArtistGameHighScore(null);

            } else if (scoreType === 'track') {

                    await apiService.deleteTrackMinigameScore(userProfile.display_name);
                    setMessage('Track game high score deleted successfully!');
                    setTrackGameHighScore(null);

            } else if (scoreType === 'all') {
                    await apiService.deleteBothMinigameScores(userProfile.display_name);
                    setMessage('All high scores deleted successfully!');
                    setArtistGameHighScore(null);
                    setTrackGameHighScore(null);
            }
        } catch (error) {
            console.error("Error removing high score:", error);
            setMessage('Error removing high score. Please try again later.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={`settings-container ${isDarkMode ? 'dark' : 'light'}`}>
            <h1>Settings</h1>
            <p>Here you can log in/log out, reset minigame scores, and change the theme.</p>

            <div className="settings-section">
                <h2>Appearance</h2>

                <div className="setting-item">
                    <span>Dark Mode</span>
                    <label className="switch">
                        <input
                            type="checkbox"
                            checked={isDarkMode}
                            onChange={toggleTheme}
                        />
                        <span className="slider round"></span>
                    </label>
                </div>
            </div>

            <div className="settings-section">
                <h2>Minigame Settings</h2>

                {userProfile ? (
                    <div className="setting-item">
                        <div>
                            <p>Logged in as: {userProfile.display_name}</p>
                            <div className="minigame-scores">
                                <div className="score-item">
                                    <p>Artist Game Best Time: {artistGameHighScore !== null ? `${artistGameHighScore} seconds` : 'No score recorded'}</p>
                                    <button
                                        onClick={() => handleRemoveHighScore('artist')}
                                        disabled={loading || artistGameHighScore === null}
                                        className="reset-button"
                                    >
                                        {loading ? 'Processing...' : 'Reset Artist Score'}
                                    </button>
                                </div>

                                <div className="score-item">
                                    <p>Track Game Best Time: {trackGameHighScore !== null ? `${trackGameHighScore} seconds` : 'No score recorded'}</p>
                                    <button
                                        onClick={() => handleRemoveHighScore('track')}
                                        disabled={loading || trackGameHighScore === null}
                                        className="reset-button"
                                    >
                                        {loading ? 'Processing...' : 'Reset Track Score'}
                                    </button>
                                </div>

                                <button
                                    onClick={() => handleRemoveHighScore('all')}
                                    disabled={loading || (artistGameHighScore === null && trackGameHighScore === null)}
                                    className="reset-all-button"
                                >
                                    {loading ? 'Processing...' : 'Reset All Scores'}
                                </button>
                            </div>
                        </div>
                    </div>
                ) : (
                    <p>Login with Spotify to manage your minigame scores</p>
                )}

                {message && <p className="message">{message}</p>}
            </div>
        </div>
    );
}

export default Settings;