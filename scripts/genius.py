import requests



def get_lyrics(song_title, access_token):
    base_url = "https://api.genius.com"
    headers = {'Authorization': f'Bearer {access_token}'}
    search_url = base_url + "/search"
    data = {'q': song_title}
    response = requests.get(search_url, headers=headers, params=data)
    
    if response.status_code != 200:
        return f"Error: Unable to fetch data from Genius API (Status code: {response.status_code})"
    
    json_response = response.json()
    song_info = None
    
    for hit in json_response['response']['hits']:
        if hit['result']['title'].lower() == song_title.lower():
            song_info = hit
            break
    
    if not song_info:
        return "Song not found"
    
    song_id = song_info['result']['id']
    song_url = base_url + f"/songs/{song_id}"
    response = requests.get(song_url, headers=headers)
    
    if response.status_code != 200:
        return f"Error: Unable to fetch song details from Genius API (Status code: {response.status_code})"
    
    song_details = response.json()
    song_path = song_details['response']['song']['path']
    lyrics_url = "https://genius.com" + song_path

    # Fetch lyrics from the song's page
    page_response = requests.get(lyrics_url)
    if page_response.status_code != 200:
        return f"Error: Unable to fetch lyrics from Genius (Status code: {page_response.status_code})"
    
    from bs4 import BeautifulSoup
    page_html = BeautifulSoup(page_response.text, 'html.parser')
    [h.extract() for h in page_html('script')]
    
    lyrics_div = page_html.find('div', class_='lyrics')
    if lyrics_div:
        lyrics = lyrics_div.get_text()
    else:
        lyrics_div = page_html.find('div', class_='Lyrics__Container-sc-1ynbvzw-6 YYrds')
        lyrics = ''
        for div in lyrics_div:
            if div.name == 'br':
                lyrics += '\n'
            else:
                lyrics += div.get_text()
    
    return lyrics

# Example usage:
access_token = "YOUR_GENIUS_ACCESS_TOKEN"
song_title = "Your Song Title"
lyrics = get_lyrics(song_title, access_token)
print(lyrics)
